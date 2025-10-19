package siksha.wafflestudio.core.domain.community.post.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.common.validator.NullOrNotBlank
import siksha.wafflestudio.core.domain.community.board.data.Board
import siksha.wafflestudio.core.domain.community.post.data.Post
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.util.EtcUtils

data class PostCreateRequestDto(
    @JsonProperty("board_id")
    val boardId: Int,
    @field:NotBlank(message = "제목은 1자에서 200자 사이여야 합니다.")
    @field:Size(max = 200, message = "제목은 1자에서 200자 사이여야 합니다.")
    val title: String,
    @field:NotBlank(message = "내용은 1자에서 1000자 사이여야 합니다.")
    @field:Size(max = 1000, message = "내용은 1자에서 1000자 사이여야 합니다.")
    val content: String,
    val anonymous: Boolean? = false,
    val images: List<MultipartFile>?,
) {
    fun toEntity(
        user: User,
        board: Board,
        imageUrls: List<String>?,
    ): Post {
        val etcJson: String? =
            imageUrls?.let {
                EtcUtils.convertImageUrlsToEtcJson(it)
            }
        return Post(
            user = user,
            board = board,
            title = title,
            content = content,
            available = true,
            anonymous = anonymous ?: false,
            etc = etcJson,
        )
    }
}

data class PostPatchRequestDto(
    @field:NullOrNotBlank(message = "제목은 1자에서 200자 사이여야 합니다.")
    @field:Size(max = 200, message = "제목은 1자에서 200자 사이여야 합니다.")
    val title: String?,
    @field:NullOrNotBlank(message = "내용은 1자에서 1000자 사이여야 합니다.")
    @field:Size(max = 1000, message = "내용은 1자에서 1000자 사이여야 합니다.")
    val content: String?,
    val anonymous: Boolean? = false,
    val images: List<MultipartFile>?,
) {
    fun toEntity(
        post: Post,
        newImageUrls: List<String>?,
    ): Post {
        val newEtcJson: String? =
            newImageUrls?.let {
                EtcUtils.convertImageUrlsToEtcJson(it)
            }
        return Post(
            id = post.id,
            user = post.user,
            board = post.board,
            title = title ?: post.title,
            content = content ?: post.content,
            available = post.available,
            anonymous = anonymous ?: post.anonymous,
            etc = newEtcJson,
            // 아무 이미지도 안 보내면 이미지 삭제
        )
    }
}

data class CreatePostReportRequestDto(
    val reason: String,
)
