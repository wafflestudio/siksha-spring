package siksha.wafflestudio.core.repository.comment

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.comment.data.Comment
import siksha.wafflestudio.core.domain.comment.repository.CommentRepository
import siksha.wafflestudio.core.domain.post.data.Post
import siksha.wafflestudio.core.domain.user.data.User
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentTest {
    @Autowired
    lateinit var repository: CommentRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `save comment`() {
        // given
        val user = entityManager.persist(
            User(
                type = "test",
                identity = "siksha",
                nickname = "waffle",
                profileUrl = "https://siksha.wafflestudio.com/"
            )
        )

        val board = entityManager.persist(
            Board(
                name = "test",
                description = "test"
            )
        )

        val post = entityManager.persist(
            Post(
                user = user,
                board = board,
                title = "test",
                content = "test",
                available = true,
                anonymous = false,
                etc = null
            )
        )

        // when
        val comment = Comment(
            user = user,
            post = post,
            content = "test",
            available = true,
            anonymous = true
        )

        val savedComment = repository.save(comment)

        // then
        assertNotNull(savedComment)
        assertEquals(savedComment.content, comment.content)
    }

    @Test
    fun `find by post ids`() {
        // given
        val user = entityManager.persist(
            User(
                type = "test",
                identity = "siksha",
                nickname = "waffle",
                profileUrl = "https://siksha.wafflestudio.com/"
            )
        )

        val board = entityManager.persist(
            Board(
                name = "test",
                description = "test"
            )
        )

        val post = entityManager.persist(
            Post(
                user = user,
                board = board,
                title = "test",
                content = "test",
                available = true,
                anonymous = false,
                etc = null
            )
        )

        val comment = entityManager.persist(
            Comment(
                user = user,
                post = post,
                content = "test",
                available = true,
                anonymous = true
            )
        )

        //when
        val result = repository.findByPostIdIn(listOf(post.id))

        // then
        assertNotNull(result)
        assertEquals(result.size, 1)
        assertEquals(result[0].id, post.id)
    }

    @Test
    fun `count by post id`() {
        // given
        val user = entityManager.persist(
            User(
                type = "test",
                identity = "siksha",
                nickname = "waffle",
                profileUrl = "https://siksha.wafflestudio.com/"
            )
        )

        val board = entityManager.persist(
            Board(
                name = "test",
                description = "test"
            )
        )

        val post = entityManager.persist(
            Post(
                user = user,
                board = board,
                title = "test",
                content = "test",
                available = true,
                anonymous = false,
                etc = null
            )
        )

        val comment = entityManager.persist(
            Comment(
                user = user,
                post = post,
                content = "test",
                available = true,
                anonymous = true
            )
        )

        entityManager.flush()
        entityManager.clear()

        //when
        val result = repository.countCommentsByPostId(post.id)

        // then
        assertEquals(result, 1)
    }
}
