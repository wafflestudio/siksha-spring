package siksha.wafflestudio.core.domain.comment.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonNaming

data class CreateCommentRequestDto(
    @JsonProperty("post_id") val postId: Int,
    val content: String,
    val anonymous: Boolean,
)

data class PatchCommentRequestDto(
    val content: String?,
    val anonymous: Boolean?
)

data class CreateCommentReportRequestDto(
    val reason: String,
)
