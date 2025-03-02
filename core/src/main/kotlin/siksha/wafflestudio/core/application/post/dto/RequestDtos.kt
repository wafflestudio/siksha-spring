package siksha.wafflestudio.core.application.post.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.post.data.Post
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.util.EtcUtils

data class PostCreateRequestDto(
    @JsonProperty("board_id") val boardId: Int,
    val title: String,
    val content: String,
    val anonymous: Boolean?,
    val images: List<MultipartFile>?
){
    fun toEntity(user: User, board: Board, imageUrls: List<String>?): Post {
        val etcJson: String? = imageUrls?.let {
            EtcUtils.convertImageUrlsToEtcJson(it)
        }
        return Post(
            user = user,
            board = board,
            title = title,
            content = content,
            available = true,
            anonymous = anonymous ?: false,
            etc = etcJson
        )
    }
}

data class PostPatchRequestDto(
    val title: String?,
    val content: String?,
    val anonymous: Boolean?,
    val images: List<MultipartFile>?
){
    fun toEntity(post: Post, newImageUrls: List<String>?): Post {
        val newEtcJson: String? = newImageUrls?.let {
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
            etc = newEtcJson // 아무 이미지도 안 보내면 이미지 삭제
        )
    }
}

data class CreatePostReportRequestDto(
    val reason: String,
)
