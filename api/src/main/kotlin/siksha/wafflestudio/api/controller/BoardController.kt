package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.board.BoardCreateDto
import siksha.wafflestudio.core.domain.board.BoardDto
import siksha.wafflestudio.core.domain.board.service.BoardService
import java.util.*

@RestController
@RequestMapping("/community/boards")
class BoardController(
    private val boardService: BoardService,
) {
    @GetMapping
    fun getBoards(): List<BoardDto> = boardService.getBoards()

    @GetMapping("/{board_id}")
    fun getBoard(
        @PathVariable("board_id") boardId: Long,
    ): Optional<BoardDto> = Optional.ofNullable(boardService.getBoardById(boardId))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addBoard(
        request: HttpServletRequest,
        @RequestBody board: BoardCreateDto,
    ): BoardDto = boardService.addBoard(request.userId, board)
}
