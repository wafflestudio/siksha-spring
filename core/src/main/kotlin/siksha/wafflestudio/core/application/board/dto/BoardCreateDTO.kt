package siksha.wafflestudio.core.application.board.dto

import siksha.wafflestudio.core.domain.board.data.Board

data class BoardCreateDTO(
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
