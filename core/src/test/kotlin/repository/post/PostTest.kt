package siksha.wafflestudio.core.repository.post

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.post.data.Post
import siksha.wafflestudio.core.domain.post.repository.PostRepository
import siksha.wafflestudio.core.domain.user.data.User
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostTest {
    @Autowired
    lateinit var repository: PostRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `save post`() {
        // given
        val user = User(
            type = "test",
            identity = "siksha",
            nickname = "waffle",
            profileUrl = "https://siksha.wafflestudio.com/"
        )
        val persistedUser = entityManager.persist(user)

        val board = Board(
            name = "test",
            description = "test"
        )
        val persistedBoard = entityManager.persist(board)

        // when
        val post = Post(
            user = persistedUser,
            board = persistedBoard,
            title = "test",
            content = "test",
            available = true,
            anonymous = false,
            etc = null
        )

        val savedPost = repository.save(post)

        // then
        assertNotNull(savedPost)
        assertEquals(post.title, savedPost.title)
    }

    @Test
    fun `get post`() {
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

        val post = Post(
            user = user,
            board = board,
            title = "test",
            content = "test",
            available = true,
            anonymous = false,
            etc = null
        )
        val savedPost = entityManager.persist(post)

        entityManager.flush()
        entityManager.clear()

        // when
        val foundPost = repository.findById(savedPost.id).orElse(null)

        // then
        assertNotNull(foundPost)
        assertEquals(post.title, foundPost.title)
    }
}
