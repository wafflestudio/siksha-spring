package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.community.board.dto.BoardCreateDto
import siksha.wafflestudio.core.domain.community.board.dto.BoardDto
import siksha.wafflestudio.core.domain.community.board.service.BoardService
import java.util.Optional

@RestController
@RequestMapping("/community/boards")
class BoardController(
    private val boardService: BoardService,
) {
    @GetMapping
    fun getBoards(): List<BoardDto> = boardService.getBoards()

    @GetMapping("/{board_id}")
    fun getBoard(
        @PathVariable("board_id") boardId: Int,
    ): Optional<BoardDto> = Optional.ofNullable(boardService.getBoardById(boardId))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addBoard(
        request: HttpServletRequest,
        @RequestBody board: BoardCreateDto,
    ): BoardDto = boardService.addBoard(request.userId, board)
}
