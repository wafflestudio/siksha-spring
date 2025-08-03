package siksha.wafflestudio.core.repository.comment

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import siksha.wafflestudio.core.domain.community.board.data.Board
import siksha.wafflestudio.core.domain.community.comment.data.Comment
import siksha.wafflestudio.core.domain.community.comment.data.CommentReport
import siksha.wafflestudio.core.domain.community.comment.repository.CommentReportRepository
import siksha.wafflestudio.core.domain.community.post.data.Post
import siksha.wafflestudio.core.domain.user.data.User
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentReportTest {
    @Autowired
    lateinit var repository: CommentReportRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `save comment report`() {
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
        val commentReport =
            CommentReport(
                reason = "test",
                reportingUser = user,
                reportedUser = user,
                comment = comment,
            )

        val savedCommentReport = repository.save(commentReport)

        // then
        assertNotNull(savedCommentReport)
        assertEquals(savedCommentReport.reason, commentReport.reason)
    }

    @Test
    fun `count comment report`() {
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

        val commentReport =
            entityManager.persist(
                CommentReport(
                    reason = "test",
                    reportingUser = user,
                    reportedUser = user,
                    comment = comment,
                ),
            )

        entityManager.flush()
        entityManager.clear()

        // when
        val result = repository.countCommentReportByCommentId(comment.id)

        // then
        assertEquals(result, 1)
    }
}
