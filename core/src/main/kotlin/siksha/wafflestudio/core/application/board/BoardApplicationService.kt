package siksha.wafflestudio.core.application.board

import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.application.board.dto.BoardCreateDTO
import siksha.wafflestudio.core.application.board.dto.BoardDto
import siksha.wafflestudio.core.domain.board.repository.BoardRepository
import siksha.wafflestudio.core.domain.board.service.BoardDomainService
import siksha.wafflestudio.core.domain.common.exception.BoardNameAlreadyExistException
import siksha.wafflestudio.core.domain.common.exception.BoardNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidBoardFormException

@Service
class BoardApplicationService(
    private val boardRepository: BoardRepository,
    private val boardDomainService: BoardDomainService,
) {
    fun getBoards(): List<BoardDto> = boardRepository.findAll().map { BoardDto.from(it) }

    @Transactional
    fun addBoard(boardCreateDTO: BoardCreateDTO): BoardDto {
        val board = boardCreateDTO.toEntity()
        try {
            boardDomainService.validateBoard(board)
            val savedBoard = boardRepository.save(board)
            return BoardDto.from(savedBoard)
        } catch (ex: DataIntegrityViolationException) {
            throw BoardNameAlreadyExistException()
        } catch (ex: InvalidBoardFormException) {
            throw ex;
        }

    }
    fun getBoardById(id: Long): BoardDto {
        val board = boardRepository.findByIdOrNull(id) ?: throw BoardNotFoundException()
        return BoardDto.from(board)
    }
}
