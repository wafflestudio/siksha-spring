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
import siksha.wafflestudio.core.application.post.dto.GetPostsResponseDto
import siksha.wafflestudio.core.application.post.dto.PostCreateDto
import siksha.wafflestudio.core.application.post.dto.PostResponseDto
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException
import siksha.wafflestudio.core.domain.image.data.Image
import siksha.wafflestudio.core.domain.image.data.ImageCategory
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.post.repository.PostLikeRepository
import siksha.wafflestudio.core.domain.post.repository.PostReportRepository
import siksha.wafflestudio.core.domain.post.repository.PostRepository
import siksha.wafflestudio.core.domain.post.service.PostDomainService
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.s3.S3ImagePrefix
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
            val userPostLiked = postIdToPostLikes[post.id]?.any { it.user.id == userId && it.isLiked == true } ?: false
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
        val board = boardRepository.findByIdOrNull(postCreateDto.boardId) ?: throw BoardNotFoundException()

        val imageUrls = postCreateDto.images
            ?.takeIf { it.isNotEmpty() }
            ?.let { handleImageUpload(postCreateDto.boardId, userId, it) }

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

    private fun handleImageUpload(boardId: Long, userId: Long, images: List<MultipartFile>): List<String> {
        val nameKey = generateImageNameKey(boardId, userId)
        val uploadFiles = s3Service.uploadFiles(images, S3ImagePrefix.POST, nameKey)

        imageRepository.saveAll(
            uploadFiles.map {
                Image(
                    key = it.key,
                    category = ImageCategory.POST,
                    userId = userId,
                    isDeleted = false
                )
            }
        )
        return uploadFiles.map { it.url }
    }
}

