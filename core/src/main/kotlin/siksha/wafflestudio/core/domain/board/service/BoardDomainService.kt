package siksha.wafflestudio.core.domain.board.service

import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.common.exception.InvalidBoardFormException

@Service
class BoardDomainService {
    fun validateBoard(board: Board) {
        val errors = mutableListOf<String>()
        if (board.name.isBlank() || board.name.length > 200) {
            errors.add("Name must be between 1 and 200 characters")
        }
        if (board.description.isBlank()) {
            errors.add("Description cannot be empty")
        }
        if (errors.isNotEmpty()) {
            throw InvalidBoardFormException(errors.toString())
        }
    }
}
