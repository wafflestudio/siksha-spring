package siksha.wafflestudio.core.domain.post.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetPostsResponseDto(
    val result: List<PostResponseDto>,
    val totalCount: Long,
    val hasNext: Boolean,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostResponseDto(
    val id: Long,
    val boardId: Long,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val nickname: String,
    val profileUrl: String?,
    val available: Boolean,
    val anonymous: Boolean,
    val etc: String?, // TODO: parse this
    val likeCnt: Int,
    val commentCnt: Int,
    val isLiked: Boolean?,
)
