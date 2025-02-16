package siksha.wafflestudio.core.application.post

import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.board.repository.BoardRepository
import siksha.wafflestudio.core.domain.comment.repository.CommentRepository
import siksha.wafflestudio.core.application.post.dto.GetPostsResponseDto
import siksha.wafflestudio.core.application.post.dto.PostCreateRequestDto
import siksha.wafflestudio.core.application.post.dto.PostPatchRequestDto
import siksha.wafflestudio.core.application.post.dto.PostResponseDto
import siksha.wafflestudio.core.domain.common.exception.*
import siksha.wafflestudio.core.domain.image.data.Image
import siksha.wafflestudio.core.domain.image.data.ImageCategory
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.post.data.Post
import siksha.wafflestudio.core.domain.post.data.PostLike
import siksha.wafflestudio.core.domain.post.repository.PostLikeRepository
import siksha.wafflestudio.core.domain.post.repository.PostReportRepository
import siksha.wafflestudio.core.domain.post.repository.PostRepository
import siksha.wafflestudio.core.domain.post.service.PostDomainService
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.s3.S3ImagePrefix
import siksha.wafflestudio.core.infrastructure.s3.S3Service
import siksha.wafflestudio.core.util.EtcUtils
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

        val postDtos = mapPostsPageWithLikesAndComments(postsPage, userId)

        return GetPostsResponseDto(
            result = postDtos,
            totalCount = postsPage.totalElements,
            hasNext = postsPage.hasNext(),
        )
    }

    fun getMyPosts(
        page: Int,
        perPage: Int,
        userId: Long,
    ): GetPostsResponseDto {
        val pageable = PageRequest.of(page-1, perPage)
        val postsPage = postRepository.findPageByUserId(userId, pageable)

        if (postsPage.isEmpty && postsPage.totalElements > 0) throw InvalidPageNumberException()

        val postDtos = mapPostsPageWithLikesAndComments(postsPage, userId)

        return GetPostsResponseDto(
            result = postDtos,
            totalCount = postsPage.totalElements,
            hasNext = postsPage.hasNext(),
        )
    }

    fun getPost(
        postId: Long,
        userId: Long?,
    ): PostResponseDto {
        val post = postRepository.findByIdOrNull(postId) ?: throw PostNotFoundException()
        return mapPostWithLikesAndComments(post, userId)
    }

    @Transactional
    fun createPost(userId: Long, postCreateRequestDto: PostCreateRequestDto): PostResponseDto {
        postDomainService.validateDto(postCreateRequestDto)
        val user = userRepository.findByIdOrNull(userId) ?: throw UnauthorizedUserException()
        val board = boardRepository.findByIdOrNull(postCreateRequestDto.boardId) ?: throw BoardNotFoundException()

        val imageUrls = postCreateRequestDto.images
            ?.takeIf { it.isNotEmpty() }
            ?.let { handleImageUpload(postCreateRequestDto.boardId, userId, it) }

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
    fun patchPost(userId: Long, postId: Long, postPatchRequestDto: PostPatchRequestDto): PostResponseDto {
        postDomainService.validatePatchDto(postPatchRequestDto)

        val post = postRepository.findByIdOrNull(postId) ?: throw PostNotFoundException()
        if (post.user.id != userId) throw NotPostOwnerException()

        // 기존 이미지 무조건 삭제 후 postPatchRequestDto.images로 대체
        post.etc?.let {
            val parsedImageUrls = EtcUtils.parseImageUrlsFromEtc(it)
            val keys = EtcUtils.getImageKeysFromUrlList(parsedImageUrls)
            imageRepository.softDeleteByKeyIn(keys)
        }

        val newImageUrls = postPatchRequestDto.images
            ?.takeIf { it.isNotEmpty() }
            ?.let { handleImageUpload(post.board.id, userId, it) }

        val newPost = runCatching {
            postRepository.save(
                postPatchRequestDto.toEntity(post, newImageUrls)
            )
        }.getOrElse {
            throw CustomNotFoundException(NotFoundItem.POST, NotFoundItem.BOARD)
        }

        return mapPostWithLikesAndComments(newPost, userId)
    }

    @Transactional
    fun deletePost(userId: Long, postId: Long) {
        val post = postRepository.findByIdOrNull(postId) ?: throw PostNotFoundException()
        if (post.user.id != userId) throw NotPostOwnerException()

        post.etc?.let {
            val parsedImageUrls = EtcUtils.parseImageUrlsFromEtc(it)
            val keys = EtcUtils.getImageKeysFromUrlList(parsedImageUrls)
            imageRepository.softDeleteByKeyIn(keys)
        }

        postRepository.deleteById(postId)
    }

    private fun mapPostsPageWithLikesAndComments(postsPage: Page<Post>, userId: Long?): List<PostResponseDto> {
        val posts = postsPage.content
        val postIdToPostLikes = postLikeRepository.findByPostIdInAndIsLikedTrue(posts.map { it.id }).groupBy { it.post.id }
        val postIdToComments = commentRepository.findByPostIdIn(posts.map { it.id }).groupBy { it.post.id }

        return posts.map { post ->
            val likeCount = postIdToPostLikes[post.id]?.size ?: 0
            val commentCount = postIdToComments[post.id]?.size ?: 0
            val isMine = post.user.id == userId
            val userPostLiked = postIdToPostLikes[post.id]?.any { it.user.id == userId } ?: false
            PostResponseDto.from(post = post, isMine = isMine, userPostLiked = userPostLiked, likeCnt = likeCount, commentCnt = commentCount)
        }
    }

    private fun mapPostWithLikesAndComments(post: Post, userId: Long?): PostResponseDto {
        val postIdToPostLike: List<PostLike> = postLikeRepository.findByPostIdAndIsLikedTrue(postId = post.id)
        val likeCount = postIdToPostLike.size
        val commentCount = commentRepository.countByPostId(postId = post.id)
        val isMine = post.user.id == userId
        val userPostLiked = postIdToPostLike.any { it.user.id == userId }

        return PostResponseDto.from(post = post, isMine = isMine, userPostLiked = userPostLiked, likeCnt = likeCount, commentCnt = commentCount)
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

