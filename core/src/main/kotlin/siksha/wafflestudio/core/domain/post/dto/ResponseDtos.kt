package siksha.wafflestudio.core.domain.post.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import siksha.wafflestudio.core.domain.post.data.Post
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
) {
    companion object {
        fun fromEntity(post: Post, likeCnt: Int, commentCnt: Int): PostResponseDto {
            return PostResponseDto(
                id = post.id,
                boardId = post.board.id,
                title = post.title,
                content = post.content,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt,
                nickname = post.user.nickname,
                profileUrl = post.user.profileUrl,
                available = post.available,
                anonymous = post.anonymous,
                etc = post.etc,
                likeCnt = likeCnt,
                commentCnt = commentCnt,
                isLiked = false,
            )
        }
    }
}
