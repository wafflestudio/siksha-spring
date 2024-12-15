package siksha.wafflestudio.core.service.board

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import siksha.wafflestudio.core.application.board.BoardApplicationService
import siksha.wafflestudio.core.application.board.dto.BoardCreateDTO
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.board.repository.BoardRepository
import siksha.wafflestudio.core.domain.board.service.BoardDomainService
import siksha.wafflestudio.core.domain.common.exception.BoardNameAlreadyExistException
import siksha.wafflestudio.core.domain.common.exception.BoardNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidBoardFormException
import kotlin.test.assertNotNull

class BoardServiceTest {
    private lateinit var repository: BoardRepository
    private lateinit var domainService: BoardDomainService
    private lateinit var service: BoardApplicationService

    @BeforeEach
    internal fun setUp() {
        repository = mockk()
        domainService = BoardDomainService()
        service = BoardApplicationService(repository, domainService)
        clearAllMocks()
    }

    @Test
    fun `should save board`() {
        // given
        val board = Board(name = "test", description = "테스트 게시판")
        every { repository.save(any()) } returns board

        // when
        val boardCreateDTO = BoardCreateDTO("test", "테스트 게시판")
        val result = service.addBoard(boardCreateDTO)

        // when
        assertNotNull(result)
        assertEquals(boardCreateDTO.name, result.name)
        assertEquals(boardCreateDTO.description, result.description)
        assertEquals(1, result.type)
        verify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `create board invalid error`() {
        // given

        // when
        val boardCreateDTO = BoardCreateDTO(name = null, description = "테스트")
        val exception = assertThrows<InvalidBoardFormException> {
            service.addBoard(boardCreateDTO)
        }

        // then
        assertEquals("[Name must be between 1 and 200 characters]", exception.message)
    }

    @Test
    fun `create board already exists error`() {
        // given
        every { repository.save(any()) } throws DataIntegrityViolationException("")

        // when
        val boardCreateDTO = BoardCreateDTO("existing", "이미 존재하는 게시판")
        val exception = assertThrows<BoardNameAlreadyExistException> {
            service.addBoard(boardCreateDTO)
        }

        // then
        assertEquals("Board name already exists", exception.message)
    }

    @Test
    fun `get all boards`() {
        // given
        val board1 = Board(name = "test", description = "테스트 게시판")
        val board2 = Board(name = "test2", description = "테스트 게시판2")
        every { repository.findAll() } returns listOf(board1, board2)

        // when
        val result = service.getBoards()

        // then
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals(board1.name, result[0].name)
        assertEquals(board2.name, result[1].name)
    }

    @Test
    fun `get a board`() {
        // given
        val board = Board(name = "test", description = "테스트 게시판")
        every { repository.findByIdOrNull(any()) } returns board

        // when
        val result = service.getBoardById(1)

        // then
        assertNotNull(result)
        assertEquals(board.name, result.name)
        assertEquals(board.description, result.description)
    }

    @Test
    fun `fail to get a board`() {
        // given
        every { repository.findByIdOrNull(3) } returns null

        // when
        val exception = assertThrows<BoardNotFoundException> {
            service.getBoardById(3)
        }

        assertEquals("Board not found", exception.message)
    }
}
