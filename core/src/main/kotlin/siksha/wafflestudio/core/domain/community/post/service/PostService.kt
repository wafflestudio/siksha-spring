package siksha.wafflestudio.core.domain.community.post.service

import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.common.exception.BoardNotFoundException
import siksha.wafflestudio.core.domain.common.exception.CustomNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidPageNumberException
import siksha.wafflestudio.core.domain.common.exception.InvalidPostReportFormException
import siksha.wafflestudio.core.domain.common.exception.NotFoundItem
import siksha.wafflestudio.core.domain.common.exception.NotPostOwnerException
import siksha.wafflestudio.core.domain.common.exception.PostAlreadyReportedException
import siksha.wafflestudio.core.domain.common.exception.PostNotFoundException
import siksha.wafflestudio.core.domain.common.exception.PostReportSaveFailedException
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException
import siksha.wafflestudio.core.domain.community.board.repository.BoardRepository
import siksha.wafflestudio.core.domain.community.comment.repository.CommentRepository
import siksha.wafflestudio.core.domain.community.post.data.Post
import siksha.wafflestudio.core.domain.community.post.data.PostLike
import siksha.wafflestudio.core.domain.community.post.data.PostReport
import siksha.wafflestudio.core.domain.community.post.dto.PaginatedPostsResponseDto
import siksha.wafflestudio.core.domain.community.post.dto.PostCreateRequestDto
import siksha.wafflestudio.core.domain.community.post.dto.PostPatchRequestDto
import siksha.wafflestudio.core.domain.community.post.dto.PostResponseDto
import siksha.wafflestudio.core.domain.community.post.dto.PostsReportResponseDto
import siksha.wafflestudio.core.domain.community.post.dto.PostsResponseDto
import siksha.wafflestudio.core.domain.community.post.repository.PostLikeRepository
import siksha.wafflestudio.core.domain.community.post.repository.PostReportRepository
import siksha.wafflestudio.core.domain.community.post.repository.PostRepository
import siksha.wafflestudio.core.domain.image.data.Image
import siksha.wafflestudio.core.domain.image.data.ImageCategory
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.s3.S3ImagePrefix
import siksha.wafflestudio.core.infrastructure.s3.S3Service
import siksha.wafflestudio.core.util.EtcUtils
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class PostService(
    private val postRepository: PostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val postReportRepository: PostReportRepository,
    private val commentRepository: CommentRepository,
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository,
    private val s3Service: S3Service,
) {
    fun getPosts(
        boardId: Int,
        page: Int,
        perPage: Int,
        userId: Int?,
    ): PaginatedPostsResponseDto {
        if (!boardRepository.existsById(boardId)) throw BoardNotFoundException()

        val pageable = PageRequest.of(page - 1, perPage)
        val postsPage = postRepository.findPageByBoardId(boardId, pageable)

        if (postsPage.isEmpty && postsPage.totalElements > 0) throw InvalidPageNumberException()

        val postDtos = mapPostsPageWithLikesAndComments(postsPage, userId)

        return PaginatedPostsResponseDto(
            result = postDtos,
            totalCount = postsPage.totalElements,
            hasNext = postsPage.hasNext(),
        )
    }

    fun getMyPosts(
        page: Int,
        perPage: Int,
        userId: Int,
    ): PaginatedPostsResponseDto {
        val pageable = PageRequest.of(page - 1, perPage)
        val postsPage = postRepository.findPageByUserId(userId, pageable)

        if (postsPage.isEmpty && postsPage.totalElements > 0) throw InvalidPageNumberException()

        val postDtos = mapPostsPageWithLikesAndComments(postsPage, userId)

        return PaginatedPostsResponseDto(
            result = postDtos,
            totalCount = postsPage.totalElements,
            hasNext = postsPage.hasNext(),
        )
    }

    fun getPost(
        postId: Int,
        userId: Int?,
    ): PostResponseDto {
        val post = postRepository.findByIdOrNull(postId) ?: throw PostNotFoundException()
        return mapPostWithLikesAndComments(post, userId)
    }

    @Transactional
    fun createPost(
        userId: Int,
        postCreateRequestDto: PostCreateRequestDto,
    ): PostResponseDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UnauthorizedUserException()
        val board = boardRepository.findByIdOrNull(postCreateRequestDto.board_id) ?: throw BoardNotFoundException()

        val imageUrls =
            postCreateRequestDto.images
                ?.takeIf { it.isNotEmpty() }
                ?.let { handleImageUpload(postCreateRequestDto.board_id, userId, it) }

        val post = postCreateRequestDto.toEntity(user = user, board = board, imageUrls = imageUrls)

        postRepository.save(post)
        return PostResponseDto.from(
            post = post,
            isMine = true,
            userPostLiked = false,
            likeCnt = 0,
            commentCnt = 0,
        )
    }

    @Transactional
    fun patchPost(
        userId: Int,
        postId: Int,
        postPatchRequestDto: PostPatchRequestDto,
    ): PostResponseDto {
        val post = postRepository.findByIdOrNull(postId) ?: throw PostNotFoundException()
        if (post.user.id != userId) throw NotPostOwnerException()

        // 기존 이미지 무조건 삭제 후 postPatchRequestDto.images로 대체
        post.etc?.let {
            val parsedImageUrls = EtcUtils.parseImageUrlsFromEtc(it)
            val keys = EtcUtils.getImageKeysFromUrlList(parsedImageUrls)
            imageRepository.softDeleteByKeyIn(keys)
        }

        val newImageUrls =
            postPatchRequestDto.images
                ?.takeIf { it.isNotEmpty() }
                ?.let { handleImageUpload(post.board.id, userId, it) }

        val newPost =
            runCatching {
                postRepository.save(
                    postPatchRequestDto.toEntity(post, newImageUrls),
                )
            }.getOrElse {
                throw CustomNotFoundException(NotFoundItem.POST, NotFoundItem.BOARD)
            }

        return mapPostWithLikesAndComments(newPost, userId)
    }

    @Transactional
    fun deletePost(
        userId: Int,
        postId: Int,
    ) {
        val post = postRepository.findByIdOrNull(postId) ?: throw PostNotFoundException()
        if (post.user.id != userId) throw NotPostOwnerException()

        post.etc?.let {
            val parsedImageUrls = EtcUtils.parseImageUrlsFromEtc(it)
            val keys = EtcUtils.getImageKeysFromUrlList(parsedImageUrls)
            imageRepository.softDeleteByKeyIn(keys)
        }

        postRepository.deleteById(postId)
    }

    fun createOrUpdatePostLike(
        userId: Int,
        postId: Int,
        isLiked: Boolean,
    ): PostResponseDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UnauthorizedUserException()
        val post = postRepository.findByIdOrNull(postId) ?: throw PostNotFoundException()

        val postLike =
            postLikeRepository.findPostLikeByPostIdAndUserId(postId, userId)
                ?: PostLike(
                    user = user,
                    post = post,
                    isLiked = isLiked,
                )

        postLike.isLiked = isLiked
        postLikeRepository.save(postLike)

        val likeCount = postLikeRepository.countPostLikesByPostIdAndLiked(postId)
        val commentCount = commentRepository.countCommentsByPostId(postId)

        return PostResponseDto.from(
            post = post,
            isMine = post.user.id == userId,
            userPostLiked = isLiked,
            likeCnt = likeCount.toInt(),
            commentCnt = commentCount,
        )
    }

    @Transactional
    fun createPostReport(
        reportingUid: Int,
        postId: Int,
        reason: String,
    ): PostsReportResponseDto {
        val reportingUser = userRepository.findByIdOrNull(reportingUid) ?: throw UnauthorizedUserException()
        val post = postRepository.findByIdOrNull(postId) ?: throw PostNotFoundException()

        if (reason.length > 200 || reason.isBlank()) {
            throw InvalidPostReportFormException()
        }

        try {
            val postReport =
                postReportRepository.save(
                    PostReport(
                        post = post,
                        reason = reason,
                        reportingUser = reportingUser,
                        reportedUser = post.user,
                    ),
                )

            // 신고 5개 이상 누적시 숨기기
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
        } catch (ex: DataIntegrityViolationException) {
            throw PostAlreadyReportedException()
        } catch (ex: Exception) {
            throw PostReportSaveFailedException()
        }
    }

    @Cacheable(value = ["popularPostCache"], key = "#likes + '-' + #createdBefore")
    fun getTrendingPosts(
        likes: Int,
        createdBefore: Int,
        userId: Int?,
    ): PostsResponseDto {
        val postsPage =
            postRepository.findTrending(
                minimumLikes = likes,
                createdDays = OffsetDateTime.now().minusDays(createdBefore.toLong()),
            )
        val postDtos = mapPostsPageWithLikesAndComments(postsPage, userId)
        return PostsResponseDto(
            result = postDtos,
        )
    }

    @Cacheable(value = ["bestPostCache"], key = "#likes")
    fun getBestPosts(
        likes: Int,
        userId: Int?,
    ): PostsResponseDto {
        val postsPage = postRepository.findBest(minimumLikes = likes)
        val postDtos = mapPostsPageWithLikesAndComments(postsPage, userId)
        return PostsResponseDto(
            result = postDtos,
        )
    }

    private fun mapPostsPageWithLikesAndComments(
        postsPage: Page<Post>,
        userId: Int?,
    ): List<PostResponseDto> {
        val posts = postsPage.content
        val postIdToPostLikes = postLikeRepository.findByPostIdInAndIsLikedTrue(posts.map { it.id }).groupBy { it.post.id }
        val postIdToComments = commentRepository.findByPostIdIn(posts.map { it.id }).groupBy { it.post.id }

        return posts.map { post ->
            val likeCount = postIdToPostLikes[post.id]?.size ?: 0
            val commentCount = postIdToComments[post.id]?.size ?: 0
            val isMine = post.user.id == userId
            val userPostLiked = postIdToPostLikes[post.id]?.any { it.user.id == userId } ?: false
            PostResponseDto.from(
                post = post,
                isMine = isMine,
                userPostLiked = userPostLiked,
                likeCnt = likeCount,
                commentCnt = commentCount,
            )
        }
    }

    private fun mapPostWithLikesAndComments(
        post: Post,
        userId: Int?,
    ): PostResponseDto {
        val postIdToPostLike: List<PostLike> = postLikeRepository.findByPostIdAndIsLikedTrue(postId = post.id)
        val likeCount = postIdToPostLike.size
        val commentCount = commentRepository.countByPostId(postId = post.id)
        val isMine = post.user.id == userId
        val userPostLiked = postIdToPostLike.any { it.user.id == userId }

        return PostResponseDto.from(
            post = post,
            isMine = isMine,
            userPostLiked = userPostLiked,
            likeCnt = likeCount,
            commentCnt = commentCount,
        )
    }

    private fun generateImageNameKey(
        boardId: Int,
        userId: Int,
    ) = "board-$boardId/user-$userId/${OffsetDateTime.now().format(
        DateTimeFormatter.ofPattern("yyMMddHHmmss"),
    )}"

    private fun handleImageUpload(
        boardId: Int,
        userId: Int,
        images: List<MultipartFile>,
    ): List<String> {
        val nameKey = generateImageNameKey(boardId, userId)
        val uploadFiles = s3Service.uploadFiles(images, S3ImagePrefix.POST, nameKey)

        imageRepository.saveAll(
            uploadFiles.map {
                Image(
                    key = it.key,
                    category = ImageCategory.POST,
                    userId = userId,
                    isDeleted = false,
                )
            },
        )
        return uploadFiles.map { it.url }
    }
}
