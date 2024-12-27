package siksha.wafflestudio.core.domain.post.service

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.comment.repository.CommentRepository
import siksha.wafflestudio.core.domain.post.dto.GetPostsResponseDto
import siksha.wafflestudio.core.domain.post.dto.PostResponseDto
import siksha.wafflestudio.core.domain.post.repository.PostLikeRepository
import siksha.wafflestudio.core.domain.post.repository.PostReportRepository
import siksha.wafflestudio.core.domain.post.repository.PostRepository

@Service
class PostService(
    private val postRepository: PostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val postReportRepository: PostReportRepository,
    private val commentRepository: CommentRepository,
){
    fun getPostsWithoutAuth(
        boardId: Long,
        page: Int,
        perPage: Int,
    ): GetPostsResponseDto {
        val pageable = PageRequest.of(page, perPage)
        val postsPage = postRepository.findPageByBoardId(boardId, pageable)
        val posts = postsPage.content
        val postIdToPostLikes = postLikeRepository.findByPostIdIn(posts.map { it.id }).groupBy { it.post.id }
        val postIdToComments = commentRepository.findByPostIdIn(posts.map { it.id }).groupBy { it.post.id }

        val postDtos = posts.map { post ->
            val likeCount = postIdToPostLikes[post.id]?.size ?: 0
            val commentCount = postIdToComments[post.id]?.size ?: 0
            PostResponseDto(
                id = post.id,
                boardId = 0L, // FIXME: post.board.id,
                title = post.title,
                content = post.content,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt,
                nickname = post.user.nickname,
                profileUrl = post.user.profileUrl,
                available = post.available,
                anonymous = post.anonymous,
                etc = post.etc,
                likeCnt = likeCount,
                commentCnt = commentCount,
                isLiked = false,
            )
        }

        return GetPostsResponseDto(
            result = postDtos,
            totalCount = postsPage.totalElements,
            hasNext = postsPage.hasNext(),
        )
    }
}
