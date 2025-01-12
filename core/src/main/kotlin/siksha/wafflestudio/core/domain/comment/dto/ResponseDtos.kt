package siksha.wafflestudio.core.domain.comment.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetCommentsResponseDto(
    val result: List<CommentResponseDto>,
    val totalCount: Long,
    val hasNext: Boolean,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CommentResponseDto(
    val id: Long,
    val postId: Long,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
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
}

