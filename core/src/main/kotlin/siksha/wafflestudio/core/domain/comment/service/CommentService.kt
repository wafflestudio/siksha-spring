package siksha.wafflestudio.core.domain.comment.service

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.comment.dto.CommentResponseDto
import siksha.wafflestudio.core.domain.comment.dto.GetCommentsResponseDto
import siksha.wafflestudio.core.domain.comment.repository.CommentLikeRepository
import siksha.wafflestudio.core.domain.comment.repository.CommentRepository

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val commentLikeRepository: CommentLikeRepository,
){
    fun getCommentsWithoutAuth(
        page: Int,
        perPage: Int,
    ): GetCommentsResponseDto {
        val pageable = PageRequest.of(page, perPage)
        val commentsPage = commentRepository.findPageByPostId(pageable)
        val comments = commentsPage.content
        val commentIdToCommentLikes = commentLikeRepository.findByCommentIdIn(comments.map { it.id }).groupBy { it.comment.id }
        val commentDtos = comments.map { comment ->
            val likeCount = commentIdToCommentLikes[comment.id]?.size ?: 0
            CommentResponseDto(
                id = comment.id,
                postId = comment.post.id,
                content = comment.content,
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt,
                nickname = comment.user.nickname,
                profileUri = comment.user.profileUrl,
                available = comment.available,
                anonymous = comment.anonymous,
                isMine = false,
                likeCnt = likeCount,
                isLiked = false,
            )
        }

        return GetCommentsResponseDto(
            result = commentDtos,
            totalCount = commentsPage.totalElements,
            hasNext = commentsPage.hasNext(),
        )
    }
}
