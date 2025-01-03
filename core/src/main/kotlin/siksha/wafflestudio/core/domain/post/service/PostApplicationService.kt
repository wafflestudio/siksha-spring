package siksha.wafflestudio.core.domain.post.service

import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.board.repository.BoardRepository
import siksha.wafflestudio.core.domain.comment.repository.CommentRepository
import siksha.wafflestudio.core.domain.common.exception.BoardNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidPageNumberException
import siksha.wafflestudio.core.domain.post.dto.GetPostsResponseDto
import siksha.wafflestudio.core.domain.post.dto.PostCreateDto
import siksha.wafflestudio.core.domain.post.dto.PostResponseDto
import siksha.wafflestudio.core.domain.post.repository.PostLikeRepository
import siksha.wafflestudio.core.domain.post.repository.PostReportRepository
import siksha.wafflestudio.core.domain.post.repository.PostRepository

@Service
class PostApplicationService(
    private val postRepository: PostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val postReportRepository: PostReportRepository,
    private val commentRepository: CommentRepository,
    private val boardRepository: BoardRepository,
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
            // val userPostLike = userId?.let { postLikeRepository.findByPostIdAndUserId(post.id, it) }
            val userPostLiked = postIdToPostLikes[post.id]?.any { it.user.id == userId } ?: false
            PostResponseDto.from(post, isMine, userPostLiked, likeCount, commentCount)
        }

        return GetPostsResponseDto(
            result = postDtos,
            totalCount = postsPage.totalElements,
            hasNext = postsPage.hasNext(),
        )
    }

    // TODO impl this
//    @Transactional
//    fun createPost(postCreateDto: PostCreateDto): PostResponseDto {
//        // TODO: upload images to s3
//    }
}
