package siksha.wafflestudio.core.domain.community.comment.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import siksha.wafflestudio.core.domain.community.comment.data.Comment
import java.time.OffsetDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetCommentsResponseDto(
    val result: List<CommentResponseDto>,
    val totalCount: Long,
    val hasNext: Boolean,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CommentResponseDto(
    val id: Int,
    val postId: Int,
    val content: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val nickname: String?,
    val profileUri: String?,
    val available: Boolean,
    val anonymous: Boolean,
    val isMine: Boolean,
    val likeCnt: Int,
    val isLiked: Boolean,
) {
    init {
        if (anonymous) check(nickname==null && profileUri==null)
    }

    companion object {
        fun of(
            comment: Comment,
            isMine: Boolean,
            likeCount: Int,
            isLiked: Boolean
        ) = CommentResponseDto(
            id = comment.id,
            postId = comment.post.id,
            content = comment.content,
            createdAt = comment.createdAt,
            updatedAt = comment.updatedAt,
            nickname = if (comment.anonymous) null else comment.user.nickname,
            profileUri = if (comment.anonymous) null else comment.user.profileUrl,
            available = comment.available,
            anonymous = comment.anonymous,
            isMine = isMine,
            likeCnt = likeCount,
            isLiked = isLiked,
        )
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CommentsReportResponseDto(
    val id: Int,
    val reason: String,
    val commentId: Int,
)
