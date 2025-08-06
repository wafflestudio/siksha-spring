package siksha.wafflestudio.core.domain.community.board.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import siksha.wafflestudio.core.domain.community.board.data.Board
import java.time.OffsetDateTime

data class BoardCreateDto(
    val name: String?,
    val description: String?,
    val type: Int = 1,
) {
    fun toEntity(): Board {
        return Board(
            name = name ?: "",
            description = description ?: "",
            type = type,
        )
    }
}

data class BoardDto
    @JsonCreator
    constructor(
        @JsonProperty("id")
        val id: Int,
        @JsonProperty("name")
        val name: String,
        @JsonProperty("description")
        val description: String,
        @JsonProperty("type")
        val type: Int,
        @JsonProperty("created_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
        val createdAt: OffsetDateTime,
        @JsonProperty("updated_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
        val updatedAt: OffsetDateTime,
    ) {
        companion object {
            fun from(board: Board): BoardDto {
                return BoardDto(
                    id = board.id,
                    name = board.name,
                    description = board.description,
                    type = board.type,
                    createdAt = board.createdAt,
                    updatedAt = board.updatedAt,
                )
            }
        }
    }
