package siksha.wafflestudio.core.service.comment

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.comment.data.*
import siksha.wafflestudio.core.domain.comment.repository.*
import siksha.wafflestudio.core.domain.comment.service.CommentService
import siksha.wafflestudio.core.domain.common.exception.CommentAlreadyReportedException
import siksha.wafflestudio.core.domain.common.exception.InvalidCommentReportFormException
import siksha.wafflestudio.core.domain.post.data.Post
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.post.repository.PostRepository
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.*

@SpringBootTest
class CommentServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var postRepository: PostRepository
    private lateinit var commentRepository: CommentRepository
    private lateinit var commentLikeRepository: CommentLikeRepository
    private lateinit var commentReportRepository: CommentReportRepository
    private lateinit var service: CommentService

    @BeforeEach
    internal fun setUp() {
        userRepository = mockk()
        postRepository = mockk()
        commentRepository = mockk()
        commentLikeRepository = mockk()
        commentReportRepository = mockk()
        service = CommentService(
            userRepository,
            postRepository,
            commentRepository,
            commentLikeRepository,
            commentReportRepository
        )
        clearAllMocks()
    }

    @Test
    fun testComments() {
        val comments = service.getCommentsWithoutAuth(1, 1, 10).result
        assert(comments.isNotEmpty())
    }

    @Test
    fun `post comment like`() {
        // given
        val userId = 1L
        val commentId = 2L
        val isLiked = true

        val user = User(
            id = userId,
            nickname = "user",
            type = "test",
            identity = "test",
        )

        val board = Board(name = "test", description = "test")

        val comment = Comment(
            id = commentId,
            post = Post(user = user, board = board, title = "title", content = "content", anonymous = false, available = true),
            content = "Test comment",
            user = user,
            anonymous = false,
            available = true,
        )

        every { userRepository.findByIdOrNull(userId) } returns user
        every { commentRepository.findByIdOrNull(commentId) } returns comment
        every { commentLikeRepository.findCommentLikeByCommentIdAndUserId(commentId, userId) } returns null
        every { commentLikeRepository.save(any()) } returns mockk()
        every { commentLikeRepository.countCommentLikesByCommentIdAndLiked(commentId) } returns 1L

        // when
        val response = service.postCommentLike(userId, commentId, isLiked)

        // then
        assertEquals(commentId, response.id)
        assertEquals(1, response.likeCnt)
        assertEquals(isLiked, response.isLiked)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { commentRepository.findByIdOrNull(commentId) }
        verify { commentLikeRepository.findCommentLikeByCommentIdAndUserId(commentId, userId) }
        verify { commentLikeRepository.countCommentLikesByCommentIdAndLiked(commentId) }
    }

    @Test
    fun `update comment like`() {
        // given
        val userId = 1L
        val commentId = 2L
        val isLiked = false

        val user = User(
            id = userId,
            nickname = "user",
            type = "test",
            identity = "test",
        )

        val board = Board(name = "test", description = "test")

        val comment = Comment(
            id = commentId,
            post = Post(user = user, board = board, title = "title", content = "content", anonymous = false, available = true),
            content = "Test comment",
            user = user,
            anonymous = false,
            available = true,
        )

        val commentLike = CommentLike(
            user = user,
            comment = comment,
            isLiked = true,
        )

        every { userRepository.findByIdOrNull(userId) } returns user
        every { commentRepository.findByIdOrNull(commentId) } returns comment
        every { commentLikeRepository.findCommentLikeByCommentIdAndUserId(commentId, userId) } returns commentLike
        every { commentLikeRepository.save(any()) } returns mockk()
        every { commentLikeRepository.countCommentLikesByCommentIdAndLiked(commentId) } returns 0L

        // when
        val response = service.postCommentLike(userId, commentId, isLiked)

        // then
        assertEquals(commentId, response.id)
        assertEquals(0, response.likeCnt)
        assertEquals(isLiked, response.isLiked)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { commentRepository.findByIdOrNull(commentId) }
        verify { commentLikeRepository.findCommentLikeByCommentIdAndUserId(commentId, userId) }
        verify { commentLikeRepository.countCommentLikesByCommentIdAndLiked(commentId) }
    }

    @Test
    fun `post comment report`() {
        // given
        val reportingUid = 1L
        val reportedUid = 2L
        val commentId = 2L
        val reason = "reason"

        val reportingUser = User(
            id = reportingUid,
            nickname = "reporting user",
            type = "test",
            identity = "test",
        )
        val reportedUser = User(
            id = reportedUid,
            nickname = "reported user",
            type = "test",
            identity = "test",
        )

        val board = Board(name = "test", description = "test")

        val comment = Comment(
            id = commentId,
            post = Post(user = reportedUser, board = board, title = "title", content = "content", anonymous = false, available = true),
            content = "Test comment",
            user = reportedUser,
            anonymous = false,
            available = true,
        )

        val commentReport = CommentReport(
            id = 100L,
            comment = comment,
            reason = reason,
            reportingUser = reportingUser,
            reportedUser = reportedUser
        )

        every { userRepository.findByIdOrNull(reportingUid) } returns reportingUser
        every { commentRepository.findByIdOrNull(commentId) } returns comment
        every { commentReportRepository.existsByCommentIdAndReportingUser(commentId, reportingUser) } returns false
        every { commentReportRepository.save(any()) } returns commentReport

        // when
        val response = service.postCommentReport(reportingUid, commentId, reason)

        // then
        assertEquals(reason, response.reason)
        assertEquals(commentId, response.commentId)

        // verify
        verify { userRepository.findByIdOrNull(reportingUid) }
        verify { commentRepository.findByIdOrNull(commentId) }
        verify { commentReportRepository.existsByCommentIdAndReportingUser(commentId, reportingUser) }
        verify { commentReportRepository.save(any()) }
    }

    @Test
    fun `comment already reported`() {
        // given
        val reportingUid = 1L
        val reportedUid = 2L
        val commentId = 2L
        val reason = "reason"

        val reportingUser = User(
            id = reportingUid,
            nickname = "reporting user",
            type = "test",
            identity = "test",
        )
        val reportedUser = User(
            id = reportedUid,
            nickname = "reported user",
            type = "test",
            identity = "test",
        )

        val board = Board(name = "test", description = "test")

        val comment = Comment(
            id = commentId,
            post = Post(user = reportedUser, board = board, title = "title", content = "content", anonymous = false, available = true),
            content = "Test comment",
            user = reportedUser,
            anonymous = false,
            available = true,
        )

        every { userRepository.findByIdOrNull(reportingUid) } returns reportingUser
        every { commentRepository.findByIdOrNull(commentId) } returns comment
        every { commentReportRepository.existsByCommentIdAndReportingUser(commentId, reportingUser) } returns true

        // then
        assertThrows<CommentAlreadyReportedException> {
            service.postCommentReport(reportingUid, commentId, reason)
        }
    }

    @Test
    fun `invalid comment report form`() {
        // given
        val reportingUid = 1L
        val reportedUid = 2L
        val commentId = 2L
        val reason = "200자 초과".repeat(100)

        val reportingUser = User(
            id = reportingUid,
            nickname = "reporting user",
            type = "test",
            identity = "test",
        )
        val reportedUser = User(
            id = reportedUid,
            nickname = "reported user",
            type = "test",
            identity = "test",
        )

        val board = Board(name = "test", description = "test")

        val comment = Comment(
            id = commentId,
            post = Post(user = reportedUser, board = board, title = "title", content = "content", anonymous = false, available = true),
            content = "Test comment",
            user = reportedUser,
            anonymous = false,
            available = true,
        )

        every { userRepository.findByIdOrNull(reportingUid) } returns reportingUser
        every { commentRepository.findByIdOrNull(commentId) } returns comment

        // then
        assertThrows<InvalidCommentReportFormException> {
            service.postCommentReport(reportingUid, commentId, reason)
        }
    }
}
