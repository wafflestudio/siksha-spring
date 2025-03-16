package siksha.wafflestudio.core.domain.post.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import siksha.wafflestudio.core.domain.post.data.Post
import java.time.OffsetDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PaginatedPostsResponseDto(
    val result: List<PostResponseDto>,
    val totalCount: Long,
    val hasNext: Boolean,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostsResponseDto @JsonCreator constructor (
    @JsonProperty("result")
    val result: List<PostResponseDto>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostResponseDto @JsonCreator constructor (
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("board_id")
    val boardId: Int,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("content")
    val content: String,
    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
    val createdAt: OffsetDateTime,
    @JsonProperty("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
    val updatedAt: OffsetDateTime,
    @JsonProperty("nickname")
    val nickname: String?,
    @JsonProperty("profile_url")
    val profileUrl: String?,
    @JsonProperty("available")
    val available: Boolean,
    @JsonProperty("anonymous")
    val anonymous: Boolean,
    @JsonProperty("is_mine")
    val isMine: Boolean,
    @JsonProperty("etc")
    val etc: String?,
    @JsonProperty("like_cnt")
    val likeCnt: Int,
    @JsonProperty("comment_cnt")
    val commentCnt: Int,
    @JsonProperty("is_liked")
    val isLiked: Boolean,
) {
    companion object {
        fun from(post: Post, isMine: Boolean, userPostLiked: Boolean, likeCnt: Int, commentCnt: Int): PostResponseDto {
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
    val id: Int,
    val reason: String,
    val postId: Int,
)
