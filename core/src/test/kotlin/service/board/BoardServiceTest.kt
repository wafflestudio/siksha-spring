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
import siksha.wafflestudio.core.domain.common.exception.BoardNameAlreadyExistException
import siksha.wafflestudio.core.domain.common.exception.BoardNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidBoardFormException
import siksha.wafflestudio.core.domain.community.board.data.Board
import siksha.wafflestudio.core.domain.community.board.dto.BoardCreateDto
import siksha.wafflestudio.core.domain.community.board.repository.BoardRepository
import siksha.wafflestudio.core.domain.community.board.service.BoardService
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import kotlin.test.assertNotNull

class BoardServiceTest {
    private lateinit var boardRepository: BoardRepository
    private lateinit var userRepository: UserRepository
    private lateinit var service: BoardService

    @BeforeEach
    internal fun setUp() {
        boardRepository = mockk()
        userRepository = mockk()
        service = BoardService(userRepository, boardRepository)
        clearAllMocks()
        every { userRepository.findByIdOrNull(any()) } returns User(1, "", "", null, "")
    }

    @Test
    fun `should save board`() {
        // given
        val board = Board(name = "test", description = "테스트 게시판")
        every { boardRepository.save(any()) } returns board

        // when
        val boardCreateDTO = BoardCreateDto("test", "테스트 게시판")
        val result = service.addBoard(1, boardCreateDTO)

        // when
        assertNotNull(result)
        assertEquals(boardCreateDTO.name, result.name)
        assertEquals(boardCreateDTO.description, result.description)
        assertEquals(1, result.type)
        verify(exactly = 1) { boardRepository.save(any()) }
    }

    @Test
    fun `create board invalid error`() {
        // given

        // when
        val boardCreateDTO = BoardCreateDto(name = null, description = "테스트")
        val exception =
            assertThrows<InvalidBoardFormException> {
                service.addBoard(1, boardCreateDTO)
            }

        // then
        assertEquals("[Name must be between 1 and 200 characters]", exception.message)
    }

    @Test
    fun `create board already exists error`() {
        // given
        every { boardRepository.save(any()) } throws DataIntegrityViolationException("")

        // when
        val boardCreateDTO = BoardCreateDto("existing", "이미 존재하는 게시판")
        val exception =
            assertThrows<BoardNameAlreadyExistException> {
                service.addBoard(1, boardCreateDTO)
            }

        // then
        assertEquals("중복된 게시판 이름이 존재합니다.", exception.message)
    }

    @Test
    fun `get all boards`() {
        // given
        val board1 = Board(name = "test", description = "테스트 게시판")
        val board2 = Board(name = "test2", description = "테스트 게시판2")
        every { boardRepository.findAll() } returns listOf(board1, board2)

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
        every { boardRepository.findByIdOrNull(any()) } returns board

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
        every { boardRepository.findByIdOrNull(3) } returns null

        // when
        val exception =
            assertThrows<BoardNotFoundException> {
                service.getBoardById(3)
            }

        assertEquals("해당 게시판을 찾을 수 없습니다.", exception.message)
    }
}
