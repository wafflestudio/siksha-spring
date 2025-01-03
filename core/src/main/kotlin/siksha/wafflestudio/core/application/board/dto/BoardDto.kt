package siksha.wafflestudio.core.application.board.dto

import siksha.wafflestudio.core.domain.board.data.Board
import java.sql.Timestamp

data class BoardDto(
    val id: Long,
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
