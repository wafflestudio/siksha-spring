package siksha.wafflestudio.core.service.post

import io.mockk.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.multipart.MultipartFile
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import siksha.wafflestudio.core.application.post.PostApplicationService
import siksha.wafflestudio.core.application.post.dto.PostCreateDto
import siksha.wafflestudio.core.domain.board.data.Board
import siksha.wafflestudio.core.domain.board.repository.BoardRepository
import siksha.wafflestudio.core.domain.comment.repository.CommentRepository
import siksha.wafflestudio.core.domain.common.exception.*
import siksha.wafflestudio.core.domain.image.data.Image
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.post.data.Post
import siksha.wafflestudio.core.domain.post.data.PostLike
import siksha.wafflestudio.core.domain.post.data.PostReport
import siksha.wafflestudio.core.domain.post.repository.PostLikeRepository
import siksha.wafflestudio.core.domain.post.repository.PostReportRepository
import siksha.wafflestudio.core.domain.post.repository.PostRepository
import siksha.wafflestudio.core.domain.post.service.PostDomainService
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.s3.S3Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

@Testcontainers
@SpringBootTest
//@ActiveProfiles("test")
class PostServiceTest {
    private lateinit var postRepository: PostRepository
    private lateinit var postLikeRepository: PostLikeRepository
    private lateinit var postReportRepository: PostReportRepository
    private lateinit var commentRepository: CommentRepository
    private lateinit var boardRepository: BoardRepository
    private lateinit var userRepository: UserRepository
    private lateinit var imageRepository: ImageRepository
    private lateinit var domainService: PostDomainService
    private lateinit var s3Service: S3Service
    private lateinit var service: PostApplicationService


    @BeforeEach
    internal fun setUp() {
        postRepository = mockk()
        postLikeRepository = mockk()
        postReportRepository = mockk()
        commentRepository = mockk()
        boardRepository = mockk()
        userRepository = mockk()
        imageRepository = mockk()
        domainService = PostDomainService()
        s3Service = mockk()
        service = PostApplicationService(
            postRepository,
            postLikeRepository,
            postReportRepository,
            commentRepository,
            boardRepository,
            userRepository,
            imageRepository,
            domainService,
            s3Service,
        )
        clearAllMocks()
    }

    @Test
    fun `create post like`() {
        // given
        val userId = 1L
        val postId = 1L
        val isLiked = true

        val user = User(
            id = userId,
            nickname = "user",
            type = "test",
            identity = "test",
        )

        val board = Board(name = "test", description = "test")

        val post = Post(
            id = postId,
            user = user,
            board = board,
            title = "title",
            content = "content",
            anonymous = false,
            available = true
        )

        every { userRepository.findByIdOrNull(userId) } returns user
        every { postRepository.findByIdOrNull(postId) } returns post
        every { postLikeRepository.findPostLikeByPostIdAndUserId(postId, userId) } returns null
        every { postLikeRepository.save(any()) } returns mockk()
        every { postLikeRepository.countPostLikesByPostIdAndLiked(postId) } returns 1L
        every { commentRepository.countCommentsByPostId(postId) } returns 0L

        // when
        val response = service.createOrUpdatePostLike(userId, postId, isLiked)

        // then
        Assertions.assertEquals(postId, response.id)
        Assertions.assertEquals(1, response.likeCnt)
        Assertions.assertEquals(isLiked, response.isLiked)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { postRepository.findByIdOrNull(postId) }
        verify { postLikeRepository.findPostLikeByPostIdAndUserId(postId, userId) }
        verify { postLikeRepository.save(any()) }
        verify { postLikeRepository.countPostLikesByPostIdAndLiked(postId) }
        verify { commentRepository.countCommentsByPostId(postId) }
    }

    @Test
    fun `update post like`() {
        // given
        val userId = 1L
        val postId = 1L
        val isLiked = false

        val user = User(
            id = userId,
            nickname = "user",
            type = "test",
            identity = "test",
        )

        val board = Board(name = "test", description = "test")

        val post = Post(
            id = postId,
            user = user,
            board = board,
            title = "title",
            content = "content",
            anonymous = false,
            available = true
        )

        val postLike = PostLike(
            user = user,
            post = post,
            isLiked = true,
        )

        every { userRepository.findByIdOrNull(userId) } returns user
        every { postRepository.findByIdOrNull(postId) } returns post
        every { postLikeRepository.findPostLikeByPostIdAndUserId(postId, userId) } returns postLike
        every { postLikeRepository.save(any()) } returns mockk()
        every { postLikeRepository.countPostLikesByPostIdAndLiked(postId) } returns 0L
        every { commentRepository.countCommentsByPostId(postId) } returns 0L

        // when
        val response = service.createOrUpdatePostLike(userId, postId, isLiked)

        // then
        Assertions.assertEquals(postId, response.id)
        Assertions.assertEquals(0, response.likeCnt)
        Assertions.assertEquals(isLiked, response.isLiked)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { postRepository.findByIdOrNull(postId) }
        verify { postLikeRepository.findPostLikeByPostIdAndUserId(postId, userId) }
        verify { postLikeRepository.save(any()) }
        verify { postLikeRepository.countPostLikesByPostIdAndLiked(postId) }
        verify { commentRepository.countCommentsByPostId(postId) }
    }

    @Test
    fun `create post report`() {
        // given
        val reportingUid = 1L
        val reportedUid = 2L
        val postId = 1L
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

        val post = Post(
            id = postId,
            user = reportedUser,
            board = board,
            title = "title",
            content = "content",
            anonymous = false,
            available = true
        )

        val postReport = PostReport(
            id = 100L,
            post = post,
            reason = reason,
            reportingUser = reportingUser,
            reportedUser = reportedUser
        )

        every { userRepository.findByIdOrNull(reportingUid) } returns reportingUser
        every { postRepository.findByIdOrNull(postId) } returns post
        every { postReportRepository.existsByPostIdAndReportingUser(postId, reportingUser) } returns false
        every { postReportRepository.save(any()) } returns postReport
        every { postReportRepository.countPostReportByPostId(postId) } returns 1

        // when
        val response = service.createPostReport(reportingUid, postId, reason)

        // then
        Assertions.assertEquals(reason, response.reason)
        Assertions.assertEquals(postId, response.postId)

        // verify
        verify { userRepository.findByIdOrNull(reportingUid) }
        verify { postRepository.findByIdOrNull(postId) }
        verify { postReportRepository.existsByPostIdAndReportingUser(postId, reportingUser) }
        verify { postReportRepository.save(any()) }
        verify { postReportRepository.countPostReportByPostId(postId) }
    }

    @Test
    fun `post already reported`() {
        // given
        val reportingUid = 1L
        val reportedUid = 2L
        val postId = 1L
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

        val post = Post(
            id = postId,
            user = reportedUser,
            board = board,
            title = "title",
            content = "content",
            anonymous = false,
            available = true
        )

        every { userRepository.findByIdOrNull(reportingUid) } returns reportingUser
        every { postRepository.findByIdOrNull(postId) } returns post
        every { postReportRepository.existsByPostIdAndReportingUser(postId, reportingUser) } returns true

        // then
        assertThrows<PostAlreadyReportedException> {
            service.createPostReport(reportingUid, postId, reason)
        }
    }

    @Test
    fun `invalid post report form`() {
        // given
        val reportingUid = 1L
        val reportedUid = 2L
        val postId = 1L
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

        val post = Post(
            id = postId,
            user = reportedUser,
            board = board,
            title = "title",
            content = "content",
            anonymous = false,
            available = true
        )

        every { userRepository.findByIdOrNull(reportingUid) } returns reportingUser
        every { postRepository.findByIdOrNull(postId) } returns post

        // then
        assertThrows<InvalidPostReportFormException> {
            service.createPostReport(reportingUid, postId, reason)
        }
    }
}
