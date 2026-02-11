package siksha.wafflestudio.core.domain.community.board.service

import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.auth.UnauthorizedUserException
import siksha.wafflestudio.core.domain.common.exception.community.BoardNameAlreadyExistException
import siksha.wafflestudio.core.domain.common.exception.community.BoardNotFoundException
import siksha.wafflestudio.core.domain.common.exception.community.BoardSaveFailedException
import siksha.wafflestudio.core.domain.common.exception.community.InvalidBoardFormException
import siksha.wafflestudio.core.domain.community.board.data.Board
import siksha.wafflestudio.core.domain.community.board.dto.BoardCreateDto
import siksha.wafflestudio.core.domain.community.board.dto.BoardDto
import siksha.wafflestudio.core.domain.community.board.repository.BoardRepository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class BoardService(
    private val userRepository: UserRepository,
    private val boardRepository: BoardRepository,
) {
    @Cacheable(value = ["boardCache"], key = "'allBoards'")
    fun getBoards(): List<BoardDto> = boardRepository.findAll().map { BoardDto.from(it) }

    // TODO: only admin can create board
    @Transactional
    fun addBoard(
        userId: Int,
        boardCreateDTO: BoardCreateDto,
    ): BoardDto {
        userRepository.findByIdOrNull(userId) ?: throw UnauthorizedUserException()
        val board = boardCreateDTO.toEntity()
        validateBoard(board)
        try {
            val savedBoard = boardRepository.save(board)
            return BoardDto.from(savedBoard)
        } catch (ex: DataIntegrityViolationException) {
            throw BoardNameAlreadyExistException()
        } catch (ex: Exception) {
            throw BoardSaveFailedException(ex.message)
        }
    }

    @Cacheable(value = ["boardCache"], key = "#id")
    fun getBoardById(id: Int): BoardDto {
        val board = boardRepository.findByIdOrNull(id) ?: throw BoardNotFoundException()
        return BoardDto.from(board)
    }

    private fun validateBoard(board: Board) {
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
