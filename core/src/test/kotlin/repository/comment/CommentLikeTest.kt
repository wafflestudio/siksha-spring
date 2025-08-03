package siksha.wafflestudio.core.repository.comment

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import siksha.wafflestudio.core.domain.community.board.data.Board
import siksha.wafflestudio.core.domain.community.comment.data.Comment
import siksha.wafflestudio.core.domain.community.comment.data.CommentLike
import siksha.wafflestudio.core.domain.community.comment.repository.CommentLikeRepository
import siksha.wafflestudio.core.domain.community.post.data.Post
import siksha.wafflestudio.core.domain.user.data.User
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentLikeTest {
    @Autowired
    lateinit var repository: CommentLikeRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `save comment like`() {
        // given
        val user =
            entityManager.persist(
                User(
                    type = "test",
                    identity = "siksha",
                    nickname = "waffle",
                    profileUrl = "https://siksha.wafflestudio.com/",
                ),
            )

        val board =
            entityManager.persist(
                Board(
                    name = "test",
                    description = "test",
                ),
            )

        val post =
            entityManager.persist(
                Post(
                    user = user,
                    board = board,
                    title = "test",
                    content = "test",
                    available = true,
                    anonymous = false,
                    etc = null,
                ),
            )

        val comment =
            entityManager.persist(
                Comment(
                    user = user,
                    post = post,
                    content = "test",
                    available = true,
                    anonymous = true,
                ),
            )

        // when
        val commentLike =
            CommentLike(
                user = user,
                comment = comment,
                isLiked = true,
            )

        val savedCommentLike = repository.save(commentLike)

        // then
        assertNotNull(savedCommentLike)
        assertTrue(savedCommentLike.isLiked)
    }

    @Test
    fun `find comment like by comment id and user id`() {
        // given
        val user =
            entityManager.persist(
                User(
                    type = "test",
                    identity = "siksha",
                    nickname = "waffle",
                    profileUrl = "https://siksha.wafflestudio.com/",
                ),
            )

        val board =
            entityManager.persist(
                Board(
                    name = "test",
                    description = "test",
                ),
            )

        val post =
            entityManager.persist(
                Post(
                    user = user,
                    board = board,
                    title = "test",
                    content = "test",
                    available = true,
                    anonymous = false,
                    etc = null,
                ),
            )

        val comment =
            entityManager.persist(
                Comment(
                    user = user,
                    post = post,
                    content = "test",
                    available = true,
                    anonymous = true,
                ),
            )

        // when
        val commentLike =
            entityManager.persist(
                CommentLike(
                    user = user,
                    comment = comment,
                    isLiked = true,
                ),
            )

        entityManager.flush()
        entityManager.clear()

        // when
        val result = repository.findCommentLikeByCommentIdAndUserId(comment.id, user.id)

        // then
        assertNotNull(result)
        assertEquals(comment.id, result.comment.id)
        assertEquals(user.id, result.user.id)
    }
}
