package siksha.wafflestudio.core.repository.post

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.post.data.Post
import siksha.wafflestudio.core.domain.post.data.PostReport
import siksha.wafflestudio.core.domain.post.repository.PostReportRepository
import siksha.wafflestudio.core.domain.user.data.User
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostReportTest {
    @Autowired
    lateinit var repository: PostReportRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `save post report`() {
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
        val postReport = PostReport(
            post = post,
            reason = "test",
            reportingUser = user,
            reportedUser = user,
        )

        val savedPostReport = repository.save(postReport)

        // then
        assertNotNull(savedPostReport)
        assertEquals(savedPostReport.reason, postReport.reason)
    }

    @Test
    fun `count post report`() {
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

        val postReport = entityManager.persist(
            PostReport(
                post = post,
                reason = "test",
                reportingUser = user,
                reportedUser = user,
            )
        )

        entityManager.flush()
        entityManager.clear()

        // when
        val result = repository.countPostReportByPostId(post.id)

        // then
        assertEquals(result, 1)
    }
}
