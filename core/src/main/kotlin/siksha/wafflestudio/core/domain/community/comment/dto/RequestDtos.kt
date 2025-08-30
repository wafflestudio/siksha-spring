package siksha.wafflestudio.core.domain.community.comment.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateCommentRequestDto(
    @JsonProperty("post_id") val postId: Int,
    val content: String,
    val anonymous: Boolean,
)

data class PatchCommentRequestDto(
    val content: String?,
    val anonymous: Boolean?,
)

data class CreateCommentReportRequestDto(
    val reason: String,
)
