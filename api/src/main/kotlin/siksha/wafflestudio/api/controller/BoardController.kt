package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Boards", description = "커뮤니티 게시판 관리 엔드포인트")
class BoardController(
    private val boardService: BoardService,
) {
    @GetMapping
    @Operation(summary = "게시판 목록 조회", description = "모든 게시판 목록을 조회합니다")
    fun getBoards(): List<BoardDto> = boardService.getBoards()

    @GetMapping("/{board_id}")
    @Operation(summary = "게시판 상세 조회", description = "특정 게시판의 상세 정보를 조회합니다")
    fun getBoard(
        @PathVariable("board_id") boardId: Int,
    ): Optional<BoardDto> = Optional.ofNullable(boardService.getBoardById(boardId))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "게시판 생성", description = "새로운 게시판을 생성합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun addBoard(
        request: HttpServletRequest,
        @RequestBody board: BoardCreateDto,
    ): BoardDto = boardService.addBoard(request.userId, board)
}
