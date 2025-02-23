package siksha.wafflestudio.api.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import siksha.wafflestudio.core.application.board.BoardApplicationService
import siksha.wafflestudio.core.application.board.dto.BoardCreateDto
import siksha.wafflestudio.core.application.board.dto.BoardDto
import java.util.*

@RestController
@RequestMapping("/community/boards")
class BoardController(
    private val boardApplicationService: BoardApplicationService,
) {
    @GetMapping
    fun getBoards(): List<BoardDto> = boardApplicationService.getBoards()

    @GetMapping("/{board_id}")
    fun getBoard(
        @PathVariable("board_id") boardId: Int,
    ): Optional<BoardDto> = Optional.ofNullable(boardApplicationService.getBoardById(boardId))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addBoard(
        @RequestBody board: BoardCreateDto,
    ): BoardDto = boardApplicationService.addBoard(board)
}
