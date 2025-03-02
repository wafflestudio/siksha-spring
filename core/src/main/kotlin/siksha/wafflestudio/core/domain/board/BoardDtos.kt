package siksha.wafflestudio.core.domain.board

import siksha.wafflestudio.core.domain.board.data.Board
import java.sql.Timestamp

data class BoardCreateDto(
    val name: String?,
    val description: String?,
    val type: Int = 1,
){
    fun toEntity(): Board {
        return Board(
            name = name ?: "",
            description = description ?: "",
            type = type,
        )
    }
}

data class BoardDto(
    val id: Int,
    val name: String,
    val description: String,
    val type: Int,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
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
