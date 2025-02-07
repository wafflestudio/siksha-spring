package siksha.wafflestudio.core.application.post.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.post.data.Post
import siksha.wafflestudio.core.domain.user.data.User

data class PostCreateDto(
    @JsonProperty("board_id")
    val boardId: Int,
    val title: String,
    val content: String,
    val anonymous: Boolean?,
    val images: List<MultipartFile>?
){
    fun toEntity(user: User, board: Board, imageUrls: List<String>?): Post {
        val etcJson: String? = imageUrls?.let {
            ObjectMapper().writeValueAsString(mapOf("images" to it))
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
