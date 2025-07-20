package siksha.wafflestudio.core.repository.post

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.post.data.Post
import siksha.wafflestudio.core.domain.post.data.PostLike
import siksha.wafflestudio.core.domain.post.repository.PostLikeRepository
import siksha.wafflestudio.core.domain.user.data.User
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostLikeTest {
    @Autowired
    lateinit var repository: PostLikeRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `save post like`() {
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
        val postLike = PostLike(
            user = user,
            post = post,
            isLiked = true,
        )

        val savedPostLike = repository.save(postLike)

        // then
        assertNotNull(savedPostLike)
        assertTrue(savedPostLike.isLiked)
    }

    @Test
    fun `get post like by post id and user id`() {
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

        val postLike = entityManager.persist(
            PostLike(
                user = user,
                post = post,
                isLiked = true
            )
        )

        entityManager.flush()
        entityManager.clear()

        // when
        val result = repository.findPostLikeByPostIdAndUserId(post.id, user.id)

        // then
        assertNotNull(result)
        assertEquals(post.id, result.post.id)
        assertEquals(user.id, result.user.id)
    }
}
