package siksha.wafflestudio.core.application.post

import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.board.repository.BoardRepository
import siksha.wafflestudio.core.domain.comment.repository.CommentRepository
import siksha.wafflestudio.core.domain.common.exception.BoardNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidPageNumberException
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException
import siksha.wafflestudio.core.domain.common.exception.PostNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidPostReportFormException
import siksha.wafflestudio.core.domain.common.exception.PostAlreadyReportedException
import siksha.wafflestudio.core.application.post.dto.GetPostsResponseDto
import siksha.wafflestudio.core.application.post.dto.PostCreateDto
import siksha.wafflestudio.core.application.post.dto.PostResponseDto
import siksha.wafflestudio.core.application.post.dto.PostsReportResponseDto
import siksha.wafflestudio.core.domain.image.data.Image
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.post.data.PostLike
import siksha.wafflestudio.core.domain.post.data.PostReport
import siksha.wafflestudio.core.domain.post.repository.PostLikeRepository
import siksha.wafflestudio.core.domain.post.repository.PostReportRepository
import siksha.wafflestudio.core.domain.post.repository.PostRepository
import siksha.wafflestudio.core.domain.post.service.PostDomainService
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.s3.S3Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class PostApplicationService(
    private val postRepository: PostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val postReportRepository: PostReportRepository,
    private val commentRepository: CommentRepository,
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository,
    private val postDomainService: PostDomainService,
    private val s3Service: S3Service,
){
    // TODO: parse etc
    fun getPosts(
        boardId: Long,
        page: Int,
        perPage: Int,
        userId: Long?,
    ): GetPostsResponseDto {
        if (!boardRepository.existsById(boardId)) throw BoardNotFoundException()

        val pageable = PageRequest.of(page-1, perPage)
        val postsPage = postRepository.findPageByBoardId(boardId, pageable)

        if (postsPage.isEmpty && postsPage.totalElements > 0) throw InvalidPageNumberException()

        val posts = postsPage.content
        val postIdToPostLikes = postLikeRepository.findByPostIdIn(posts.map { it.id }).groupBy { it.post.id }
        val postIdToComments = commentRepository.findByPostIdIn(posts.map { it.id }).groupBy { it.post.id }

        val postDtos = posts.map { post ->
            val likeCount = postIdToPostLikes[post.id]?.size ?: 0
            val commentCount = postIdToComments[post.id]?.size ?: 0
            val isMine = post.user.id == userId
            val userPostLiked = postIdToPostLikes[post.id]?.any { it.user.id == userId } ?: false
            PostResponseDto.from(post, isMine, userPostLiked, likeCount, commentCount)
        }

        return GetPostsResponseDto(
            result = postDtos,
            totalCount = postsPage.totalElements,
            hasNext = postsPage.hasNext(),
        )
    }

    @Transactional
    fun createPost(userId: Long, postCreateDto: PostCreateDto,): PostResponseDto {
        postDomainService.validateDto(postCreateDto)
        val user = userRepository.findByIdOrNull(userId) ?: throw UnauthorizedUserException()
        val board = boardRepository.findByIdOrNull(postCreateDto.board_id) ?: throw BoardNotFoundException()

        val imageUrls = handleImageUpload(postCreateDto.board_id, userId, postCreateDto.images)

        val post = postCreateDto.toEntity(user = user, board = board, imageUrls = imageUrls)

        postRepository.save(post)
        return PostResponseDto.from(
            post = post,
            isMine = true,
            userPostLiked = false,
            likeCnt = 0,
            commentCnt = 0,
        )
    }

    private fun generateImageNameKey(boardId: Long, userId: Long) = "board-${boardId}/user-$userId/${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))}"

    private fun handleImageUpload(boardId: Long, userId: Long, images: List<MultipartFile>?): List<String>? {
        if (images.isNullOrEmpty()) return null

        val prefix = "post-images"
        val nameKey = generateImageNameKey(boardId, userId)
        val urlsAndKeys = s3Service.uploadFiles(images, prefix, nameKey)
        imageRepository.saveAll(
            urlsAndKeys.second.map { key ->
                Image(
                    key = key,
                    category = "POST",
                    userId = userId,
                    isDeleted = false
                )
            }
        )
        return urlsAndKeys.first
    }

    fun createOrUpdatePostLike(
        userId: Long,
        postId: Long,
        isLiked: Boolean,
    ): PostResponseDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UnauthorizedUserException()
        val post = postRepository.findByIdOrNull(postId) ?: throw PostNotFoundException()

        var postLike = postLikeRepository.findPostLikeByPostIdAndUserId(postId, userId)

        if (postLike == null) {
            postLike =
                PostLike(
                    user = user,
                    post = post,
                    isLiked = isLiked,
                )
        } else {
            postLike.isLiked = isLiked
        }

        postLikeRepository.save(postLike)

        val likeCount = postLikeRepository.countPostLikesByPostIdAndLiked(postId)
        val commentCount = commentRepository.countCommentsByPostId(postId)

        return PostResponseDto.from(
            post = post,
            isMine = post.user.id == userId,
            userPostLiked = isLiked,
            likeCnt = likeCount.toInt(),
            commentCnt = commentCount.toInt(),
        )
    }

    @Transactional
    fun createPostReport(
        reportingUid: Long,
        postId: Long,
        reason: String,
    ): PostsReportResponseDto {
        val reportingUser = userRepository.findByIdOrNull(reportingUid) ?: throw UnauthorizedUserException()
        val post = postRepository.findByIdOrNull(postId) ?: throw PostNotFoundException()

        if (reason.length > 200 || reason.isBlank()) {
            throw InvalidPostReportFormException()
        }
        if (postReportRepository.existsByPostIdAndReportingUser(postId, reportingUser)) {
            throw PostAlreadyReportedException()
        }

        val postReport = postReportRepository.save(
            PostReport(
                post = post,
                reason = reason,
                reportingUser = reportingUser,
                reportedUser = post.user,
            )
        )

        //신고 5개 이상 누적시 숨기기
        val postReportCount = postReportRepository.countPostReportByPostId(postId)
        if (postReportCount >= 5 && post.available) {
            post.available = false
            postRepository.save(post)
        }

        return PostsReportResponseDto(
            id = postReport.id,
            reason = postReport.reason,
            postId = postReport.post.id,
        )
    }
}
