package siksha.wafflestudio.core.application.post.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import siksha.wafflestudio.core.domain.post.data.Post
import siksha.wafflestudio.core.domain.post.data.PostLike
import siksha.wafflestudio.core.domain.user.data.User
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
    val nickname: String?,
    val profileUrl: String?,
    val available: Boolean,
    val anonymous: Boolean,
    val isMine: Boolean,
    val etc: String?,
    val likeCnt: Int,
    val commentCnt: Int,
    val isLiked: Boolean,
) {
    companion object {
        fun from(post: Post, isMine: Boolean, userPostLiked: Boolean, likeCnt: Int, commentCnt: Int,): PostResponseDto {
            return PostResponseDto(
                id = post.id,
                boardId = post.board.id,
                title = post.title,
                content = post.content,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt,
                nickname = if (post.anonymous) null else post.user.nickname,
                profileUrl = if (post.anonymous) null else post.user.profileUrl,
                available = post.available,
                anonymous = post.anonymous,
                isMine = isMine,
                etc = post.etc,
                likeCnt = likeCnt,
                commentCnt = commentCnt,
                isLiked = userPostLiked,
            )
        }
    }
    init {
        if (anonymous) check(nickname==null && profileUrl==null)
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostsReportResponseDto(
    val id: Long,
    val reason: String,
    val postId: Long,
)
