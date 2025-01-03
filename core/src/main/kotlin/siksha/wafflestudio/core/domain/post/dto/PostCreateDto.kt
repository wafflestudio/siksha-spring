package siksha.wafflestudio.core.domain.post.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.post.data.Post
import siksha.wafflestudio.core.domain.user.data.User
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PostCreateDto(
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
