package siksha.wafflestudio.core.repository.board

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.jdbc.Sql
import siksha.wafflestudio.core.domain.community.board.data.Board
import siksha.wafflestudio.core.domain.community.board.repository.BoardRepository
import kotlin.test.assertNotNull

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BoardTest {
    @Autowired
    lateinit var repository: BoardRepository

    @Test
    fun `save board`() {
        // when
        val board = Board(name = "Test", description = "테스트 게시판")
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

    @Test
    fun `delete board`() {
        // given
        val savedBoard = repository.save(Board(name = "Test", description = "테스트 게시판"))

        // when
        repository.deleteById(savedBoard.id)
        val deletedBoard = repository.findByIdOrNull(savedBoard.id)

        // then
        assertNull(deletedBoard)
    }

    @Test
    @Sql("/data/v001.sql")
    fun `existsByName should return true for existing board`() {
        // when
        val exists = repository.existsByName("자유게시판")

        // then
        assertTrue(exists)
    }

    @Test
    @Sql("/data/v001.sql")
    fun `existsByName should return false for non-existing board`() {
        // when
        val exists = repository.existsByName("없는 게시판")

        // then
        assertFalse(exists)
    }
}
