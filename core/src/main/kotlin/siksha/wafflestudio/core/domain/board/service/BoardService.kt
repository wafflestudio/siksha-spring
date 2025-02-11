package siksha.wafflestudio.core.domain.board.service

import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.board.BoardCreateDto
import siksha.wafflestudio.core.domain.board.BoardDto
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.board.repository.BoardRepository
import siksha.wafflestudio.core.domain.common.exception.*
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class BoardService(
    private val userRepository: UserRepository,
    private val boardRepository: BoardRepository,
) {
    fun getBoards(): List<BoardDto> = boardRepository.findAll().map { BoardDto.from(it) }

    // TODO: only admin can create board
    @Transactional
    fun addBoard(userId: Long, boardCreateDTO: BoardCreateDto): BoardDto {
        userRepository.findByIdOrNull(userId) ?: throw UnauthorizedUserException()
        val board = boardCreateDTO.toEntity()
        validateBoard(board)
        try {
            val savedBoard = boardRepository.save(board)
            return BoardDto.from(savedBoard)
        } catch (ex: DataIntegrityViolationException) {
            throw BoardNameAlreadyExistException()
        } catch (ex: Exception) {
            throw BoardSaveFailedException(ex.message);
        }
    }

    fun getBoardById(id: Long): BoardDto {
        val board = boardRepository.findByIdOrNull(id) ?: throw BoardNotFoundException()
        return BoardDto.from(board)
    }

    fun validateBoard(board: Board) {
        val errors = mutableListOf<String>()
        if (board.name.isBlank() || board.name.length > 200) {
            errors.add("Name must be between 1 and 200 characters")
        }
        if (board.description.isBlank()) {
            errors.add("Description cannot be empty")
        }
        if (errors.isNotEmpty()) {
            throw InvalidBoardFormException("$errors")
        }
    }
}
