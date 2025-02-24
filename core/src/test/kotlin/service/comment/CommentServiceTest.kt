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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

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
    fun `get comment without auth`() {
        // given
        val postId = 1
        val page = 1
        val perPage = 10
        val totalCount = 1L

        val user = User(
            id = 1,
            nickname = "user",
            type = "test",
            identity = "test",
        )

        val board = Board(name = "test", description = "test")

        val comment = Comment(
            id = 1,
            post = Post(id = postId, user =  user, board = board, title = "title", content = "content", anonymous = false, available = true),
            content = "Test comment",
            user = user,
            anonymous = false,
            available = true,
        )

        val pageable = PageRequest.of(page-1, perPage)

        every { commentRepository.findPageByPostId(postId, pageable) } returns PageImpl(listOf(comment), pageable, totalCount)
        every { commentLikeRepository.findByCommentIdInAndIsLiked(any()) } returns emptyList()

        //when
        val response = service.getCommentsWithoutAuth(postId, page, perPage)

        // then
        assertEquals(totalCount, response.totalCount)
        assertEquals(false, response.hasNext)
        assertEquals(totalCount, response.result.size.toLong())
        assertEquals(comment.id, response.result.first().id)

        // verify
        verify { commentRepository.findPageByPostId(postId, pageable) }
        verify { commentLikeRepository.findByCommentIdInAndIsLiked(any()) }
    }

    @Test
    fun `create comment like`() {
        // given
        val userId = 1
        val commentId = 2
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
        every { commentLikeRepository.countCommentLikesByCommentIdAndIsLiked(commentId) } returns 1L

        // when
        val response = service.createOrUpdateCommentLike(userId, commentId, isLiked)

        // then
        assertEquals(commentId, response.id)
        assertEquals(1, response.likeCnt)
        assertEquals(isLiked, response.isLiked)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { commentRepository.findByIdOrNull(commentId) }
        verify { commentLikeRepository.findCommentLikeByCommentIdAndUserId(commentId, userId) }
        verify { commentLikeRepository.countCommentLikesByCommentIdAndIsLiked(commentId) }
    }

    @Test
    fun `update comment like`() {
        // given
        val userId = 1
        val commentId = 2
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
        every { commentLikeRepository.countCommentLikesByCommentIdAndIsLiked(commentId) } returns 0L

        // when
        val response = service.createOrUpdateCommentLike(userId, commentId, isLiked)

        // then
        assertEquals(commentId, response.id)
        assertEquals(0, response.likeCnt)
        assertEquals(isLiked, response.isLiked)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { commentRepository.findByIdOrNull(commentId) }
        verify { commentLikeRepository.findCommentLikeByCommentIdAndUserId(commentId, userId) }
        verify { commentLikeRepository.countCommentLikesByCommentIdAndIsLiked(commentId) }
    }

    @Test
    fun `create comment report`() {
        // given
        val reportingUid = 1
        val reportedUid = 2
        val commentId = 2
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
            id = 100,
            comment = comment,
            reason = reason,
            reportingUser = reportingUser,
            reportedUser = reportedUser
        )

        every { userRepository.findByIdOrNull(reportingUid) } returns reportingUser
        every { commentRepository.findByIdOrNull(commentId) } returns comment
        every { commentReportRepository.existsByCommentIdAndReportingUser(commentId, reportingUser) } returns false
        every { commentReportRepository.save(any()) } returns commentReport
        every { commentReportRepository.countCommentReportByCommentId(commentId) } returns 1

        // when
        val response = service.createCommentReport(reportingUid, commentId, reason)

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
        val reportingUid = 1
        val reportedUid = 2
        val commentId = 2
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
            service.createCommentReport(reportingUid, commentId, reason)
        }
    }

    @Test
    fun `invalid comment report form`() {
        // given
        val reportingUid = 1
        val reportedUid = 2
        val commentId = 2
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
            service.createCommentReport(reportingUid, commentId, reason)
        }
    }
}
