package siksha.wafflestudio.core.repository.board

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.board.repository.BoardRepository
import kotlin.test.assertNotNull

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BoardTest {
    @Autowired
    lateinit var repository: BoardRepository

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `save board`() {
        // when
        val board = Board(name="Test", description = "테스트 게시판")
        val savedBoard = repository.save(board)

        // then
        assertNotNull(savedBoard)
        assertEquals(board.name, savedBoard.name)
    }

    @Test
    fun `get board`() {
        // given
        val savedBoard = repository.save(Board(name = "Test", description = "테스트 게시판"))

        // when
        val retrievedBoard = repository.findByIdOrNull(savedBoard.id)

        // then
        assertNotNull(retrievedBoard)
        assertEquals(savedBoard.name, retrievedBoard.name)
        assertEquals(savedBoard.description, retrievedBoard.description)
    }
}
