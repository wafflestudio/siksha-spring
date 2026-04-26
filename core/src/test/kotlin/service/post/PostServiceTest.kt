package siksha.wafflestudio.core.service.post

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import siksha.wafflestudio.core.domain.common.exception.BoardNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidPostReportFormException
import siksha.wafflestudio.core.domain.common.exception.NotPostOwnerException
import siksha.wafflestudio.core.domain.common.exception.PostAlreadyReportedException
import siksha.wafflestudio.core.domain.common.exception.PostNotFoundException
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException
import siksha.wafflestudio.core.domain.community.board.data.Board
import siksha.wafflestudio.core.domain.community.board.repository.BoardRepository
import siksha.wafflestudio.core.domain.community.comment.repository.CommentRepository
import siksha.wafflestudio.core.domain.community.post.data.Post
import siksha.wafflestudio.core.domain.community.post.data.PostLike
import siksha.wafflestudio.core.domain.community.post.data.PostReport
import siksha.wafflestudio.core.domain.community.post.dto.PostCreateRequestDto
import siksha.wafflestudio.core.domain.community.post.dto.PostPatchRequestDto
import siksha.wafflestudio.core.domain.community.post.repository.PostLikeRepository
import siksha.wafflestudio.core.domain.community.post.repository.PostReportRepository
import siksha.wafflestudio.core.domain.community.post.repository.PostRepository
import siksha.wafflestudio.core.domain.community.post.service.PostService
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.imageupload.ImageUploadUseCase
import kotlin.test.assertEquals

class PostServiceTest {
    private lateinit var postRepository: PostRepository
    private lateinit var postLikeRepository: PostLikeRepository
    private lateinit var postReportRepository: PostReportRepository
    private lateinit var commentRepository: CommentRepository
    private lateinit var boardRepository: BoardRepository
    private lateinit var userRepository: UserRepository
    private lateinit var imageRepository: ImageRepository
    private lateinit var imageUploadUseCase: ImageUploadUseCase
    private lateinit var service: PostService

    @BeforeEach
    internal fun setUp() {
        postRepository = mockk()
        postLikeRepository = mockk()
        postReportRepository = mockk()
        commentRepository = mockk()
        boardRepository = mockk()
        userRepository = mockk()
        imageRepository = mockk()
        imageUploadUseCase = mockk()
        service =
            PostService(
                postRepository,
                postLikeRepository,
                postReportRepository,
                commentRepository,
                boardRepository,
                userRepository,
                imageRepository,
                imageUploadUseCase,
            )
        clearAllMocks()
    }

    @Test
    fun `get posts`() {
        // given
        val userId = 1
        val boardId = 1

        val page = 1
        val perPage = 10

        val user =
            User(
                id = userId,
                nickname = "user",
                type = "test",
                identity = "test",
            )

        val board = Board(id = boardId, name = "test", description = "test")

        val post =
            Post(
                user = user,
                board = board,
                title = "test",
                content = "test post",
                available = true,
                anonymous = false,
            )

        val pageable = PageRequest.of(page - 1, perPage)

        every { boardRepository.existsById(boardId) } returns true
        every { postRepository.findPageByBoardId(boardId, pageable) } returns PageImpl(listOf(post), pageable, 1)
        every { postLikeRepository.findByPostIdInAndIsLikedTrue(any()) } returns emptyList()
        every { commentRepository.findByPostIdIn(any()) } returns emptyList()

        // when
        val response = service.getPosts(boardId, page, perPage, userId)

        // then
        assertEquals(1, response.totalCount)
        assertEquals(false, response.hasNext)
        assertEquals(1, response.result.size)
        assertEquals("test post", response.result[0].content)

        // verify
        verify { boardRepository.existsById(boardId) }
        verify { postRepository.findPageByBoardId(boardId, pageable) }
        verify { postLikeRepository.findByPostIdInAndIsLikedTrue(any()) }
        verify { commentRepository.findByPostIdIn(any()) }
    }

    @Test
    fun `create post unauthorized`() {
        // given
        every { userRepository.findByIdOrNull(any()) } returns null
        // when
        val dto = PostCreateRequestDto(board_id = 1, title = "test", content = "siksha fighting", anonymous = null, images = null)
        val exception =
            assertThrows<UnauthorizedUserException> {
                service.createPost(userId = 1, postCreateRequestDto = dto)
            }
        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.httpStatus)
    }

    @Test
    fun `create post invalid board`() {
        // given
        val userId = 1
        val user = User(id = userId, type = "test", identity = "siksha", nickname = "waffle")

        every { userRepository.findByIdOrNull(userId) } returns user
        every { boardRepository.findByIdOrNull(any()) } returns null
        // when
        val dto = PostCreateRequestDto(board_id = 1, title = "test", content = "siksha fighting", anonymous = null, images = null)
        val exception =
            assertThrows<BoardNotFoundException> {
                service.createPost(userId = userId, postCreateRequestDto = dto)
            }

        // then
        verify { userRepository.findByIdOrNull(userId) }
        verify { boardRepository.findByIdOrNull(any()) }
    }

    @Test
    fun `create post anonymous without image`() {
        // given
        val userId = 1
        val user =
            User(id = userId, type = "test", identity = "siksha", nickname = "waffle", profileUrl = "https://siksha.wafflestudio.com/")

        val boardId = 2
        val board = Board(id = boardId, name = "test board", description = "test")

        every { userRepository.findByIdOrNull(userId) } returns user
        every { boardRepository.findByIdOrNull(boardId) } returns board
        every { postRepository.save(any()) } returns mockk()

        // when
        val dto = PostCreateRequestDto(board_id = boardId, title = "test", content = "siksha fighting", anonymous = true, images = null)
        val response = service.createPost(userId, dto)

        // then
        assertEquals(boardId, response.boardId)
        assertEquals("test", response.title)
        assertEquals("siksha fighting", response.content)
        assertEquals(null, response.nickname)
        assertEquals(null, response.profileUrl)
        assertEquals(true, response.isMine)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { boardRepository.findByIdOrNull(boardId) }
    }

    @Test
    fun `create post not anonymous without image`() {
        // given
        val userId = 1
        val user =
            User(id = userId, type = "test", identity = "siksha", nickname = "waffle", profileUrl = "https://siksha.wafflestudio.com/")

        val boardId = 2
        val board = Board(id = boardId, name = "test board", description = "test")

        every { userRepository.findByIdOrNull(userId) } returns user
        every { boardRepository.findByIdOrNull(boardId) } returns board
        every { postRepository.save(any()) } returns mockk()

        // when
        val dto = PostCreateRequestDto(board_id = boardId, title = "test", content = "siksha fighting", anonymous = null, images = null)
        val response = service.createPost(userId, dto)

        // then
        assertEquals("waffle", response.nickname)
        assertEquals("https://siksha.wafflestudio.com/", response.profileUrl)
        assertEquals(true, response.isMine)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { boardRepository.findByIdOrNull(boardId) }
    }

    @Test
    fun `create post like`() {
        // given
        val userId = 1
        val postId = 1
        val isLiked = true
        val user =
            User(
                id = userId,
                nickname = "user",
                type = "test",
                identity = "test",
            )

        val board = Board(name = "test", description = "test")

        val post =
            Post(
                id = postId,
                user = user,
                board = board,
                title = "title",
                content = "content",
                anonymous = false,
                available = true,
            )

        every { userRepository.findByIdOrNull(userId) } returns user
        every { postRepository.findByIdOrNull(postId) } returns post
        every { postLikeRepository.findPostLikeByPostIdAndUserId(postId, userId) } returns null
        every { postLikeRepository.save(any()) } returns mockk()
        every { postLikeRepository.countPostLikesByPostIdAndLiked(postId) } returns 1
        every { commentRepository.countCommentsByPostId(postId) } returns 0

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
        val userId = 1
        val postId = 1
        val isLiked = false

        val user =
            User(
                id = userId,
                nickname = "user",
                type = "test",
                identity = "test",
            )

        val board = Board(name = "test", description = "test")

        val post =
            Post(
                id = postId,
                user = user,
                board = board,
                title = "title",
                content = "content",
                anonymous = false,
                available = true,
            )

        val postLike =
            PostLike(
                user = user,
                post = post,
                isLiked = true,
            )

        every { userRepository.findByIdOrNull(userId) } returns user
        every { postRepository.findByIdOrNull(postId) } returns post
        every { postLikeRepository.findPostLikeByPostIdAndUserId(postId, userId) } returns postLike
        every { postLikeRepository.save(any()) } returns mockk()
        every { postLikeRepository.countPostLikesByPostIdAndLiked(postId) } returns 0
        every { commentRepository.countCommentsByPostId(postId) } returns 0

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
        val reportingUid = 1
        val reportedUid = 2
        val postId = 1
        val reason = "reason"

        val reportingUser =
            User(
                id = reportingUid,
                nickname = "reporting user",
                type = "test",
                identity = "test",
            )
        val reportedUser =
            User(
                id = reportedUid,
                nickname = "reported user",
                type = "test",
                identity = "test",
            )

        val board = Board(name = "test", description = "test")

        val post =
            Post(
                id = postId,
                user = reportedUser,
                board = board,
                title = "title",
                content = "content",
                anonymous = false,
                available = true,
            )

        val postReport =
            PostReport(
                id = 100,
                post = post,
                reason = reason,
                reportingUser = reportingUser,
                reportedUser = reportedUser,
            )

        every { userRepository.findByIdOrNull(reportingUid) } returns reportingUser
        every { postRepository.findByIdOrNull(postId) } returns post
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
        verify { postReportRepository.save(any()) }
        verify { postReportRepository.countPostReportByPostId(postId) }
    }

    @Test
    fun `post already reported`() {
        // given
        val reportingUid = 1
        val reportedUid = 2
        val postId = 1
        val reason = "reason"

        val reportingUser =
            User(
                id = reportingUid,
                nickname = "reporting user",
                type = "test",
                identity = "test",
            )
        val reportedUser =
            User(
                id = reportedUid,
                nickname = "reported user",
                type = "test",
                identity = "test",
            )

        val board = Board(name = "test", description = "test")

        val post =
            Post(
                id = postId,
                user = reportedUser,
                board = board,
                title = "title",
                content = "content",
                anonymous = false,
                available = true,
            )

        every { userRepository.findByIdOrNull(reportingUid) } returns reportingUser
        every { postRepository.findByIdOrNull(postId) } returns post
        every { postReportRepository.save(any()) } throws DataIntegrityViolationException("")

        // then
        assertThrows<PostAlreadyReportedException> {
            service.createPostReport(reportingUid, postId, reason)
        }
    }

    @Test
    fun `invalid post report form`() {
        // given
        val reportingUid = 1
        val reportedUid = 2
        val postId = 1
        val reason = "200자 초과".repeat(100)

        val reportingUser =
            User(
                id = reportingUid,
                nickname = "reporting user",
                type = "test",
                identity = "test",
            )
        val reportedUser =
            User(
                id = reportedUid,
                nickname = "reported user",
                type = "test",
                identity = "test",
            )

        val board = Board(name = "test", description = "test")

        val post =
            Post(
                id = postId,
                user = reportedUser,
                board = board,
                title = "title",
                content = "content",
                anonymous = false,
                available = true,
            )

        every { userRepository.findByIdOrNull(reportingUid) } returns reportingUser
        every { postRepository.findByIdOrNull(postId) } returns post

        // then
        assertThrows<InvalidPostReportFormException> {
            service.createPostReport(reportingUid, postId, reason)
        }
    }

    @Test
    fun `get my posts`() {
        // given
        val myId = 1
        val otherUserId = 2
        val boardId = 1

        val page = 1
        val perPage = 10

        val me =
            User(
                id = myId,
                nickname = "me",
                type = "test",
                identity = "test",
            )
        val otherUser =
            User(
                id = otherUserId,
                nickname = "bob",
                type = "test",
                identity = "test",
            )

        val board = Board(id = boardId, name = "test", description = "test")

        val myPost =
            Post(
                user = me,
                board = board,
                title = "test",
                content = "test post",
                available = true,
                anonymous = false,
            )
        val otherUserPost =
            Post(
                user = otherUser,
                board = board,
                title = "test",
                content = "other user's post",
                available = true,
                anonymous = false,
            )

        val pageable = PageRequest.of(page - 1, perPage)

        every { postRepository.findPageByUserId(boardId, pageable) } returns PageImpl(listOf(myPost), pageable, 1)
        every { postLikeRepository.findByPostIdInAndIsLikedTrue(any()) } returns emptyList()
        every { commentRepository.findByPostIdIn(any()) } returns emptyList()

        // when
        val response = service.getMyPosts(page, perPage, myId)

        // then
        assertEquals(1, response.totalCount)
        assertEquals(false, response.hasNext)
        assertEquals(1, response.result.size)
        assertEquals("me", response.result[0].nickname)

        // verify
        verify { postRepository.findPageByUserId(boardId, pageable) }
        verify { postLikeRepository.findByPostIdInAndIsLikedTrue(any()) }
        verify { commentRepository.findByPostIdIn(any()) }
    }

    @Test
    fun `get a post not found`() {
        // given
        val notFoundPostId = 1
        every { postRepository.findByIdOrNull(notFoundPostId) } returns null
        // when
        val exception =
            assertThrows<PostNotFoundException> {
                service.getPost(notFoundPostId, null)
            }

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.httpStatus)
        assertEquals("해당 글을 찾을 수 없습니다.", exception.errorMessage)
    }

    @Test
    fun `get a post`() {
        // given
        val userId = 1
        val user =
            User(id = userId, type = "test", identity = "siksha", nickname = "waffle", profileUrl = "https://siksha.wafflestudio.com/")

        val boardId = 2
        val board = Board(id = boardId, name = "test board", description = "test")

        val postId = 3

        val post =
            Post(
                id = postId,
                user = user,
                board = board,
                title = "test",
                content = "test",
                available = true,
                anonymous = false,
                etc = null,
            )
        every { postRepository.findByIdOrNull(postId) } returns post
        every { postLikeRepository.findByPostIdAndIsLikedTrue(postId) } returns emptyList()
        every { commentRepository.countByPostId(postId) } returns 0

        // when
        val response = service.getPost(postId, null)

        // then
        assertEquals("test", response.title)
        assertEquals(postId, response.id)

        // verify
        verify { postRepository.findByIdOrNull(postId) }
        verify { postLikeRepository.findByPostIdAndIsLikedTrue(postId) }
        verify { commentRepository.countByPostId(postId) }
    }

    @Test
    fun `delete a post not owner`() {
        // given
        val userId = 1
        val user =
            User(id = userId, type = "test", identity = "siksha", nickname = "waffle", profileUrl = "https://siksha.wafflestudio.com/")

        val otherUserId = 10000
        val otherUser =
            User(id = otherUserId, type = "test", identity = "siksha", nickname = "waffle", profileUrl = "https://siksha.wafflestudio.com/")

        val boardId = 2
        val board = Board(id = boardId, name = "test board", description = "test")

        val postId = 3

        val post =
            Post(
                id = postId,
                user = user,
                board = board,
                title = "test",
                content = "test",
                available = true,
                anonymous = false,
                etc = null,
            )
        every { postRepository.findByIdOrNull(postId) } returns post

        // when
        val exception =
            assertThrows<NotPostOwnerException> {
                service.deletePost(otherUserId, postId)
            }

        // then
        assertEquals(HttpStatus.FORBIDDEN, exception.httpStatus)
    }

    @Test
    fun `patch post not owner`() {
        // given
        val userId = 1
        val user =
            User(id = userId, type = "test", identity = "siksha", nickname = "waffle", profileUrl = "https://siksha.wafflestudio.com/")

        val otherUserId = 10000
        val otherUser =
            User(id = otherUserId, type = "test", identity = "siksha", nickname = "waffle", profileUrl = "https://siksha.wafflestudio.com/")

        val boardId = 2
        val board = Board(id = boardId, name = "test board", description = "test")

        val postId = 3

        val post =
            Post(
                id = postId,
                user = user,
                board = board,
                title = "test",
                content = "test",
                available = true,
                anonymous = false,
                etc = null,
            )

        every { postRepository.findByIdOrNull(postId) } returns post

        // when
        val exception =
            assertThrows<NotPostOwnerException> {
                service.patchPost(otherUserId, postId, PostPatchRequestDto(null, null, null, null))
            }
        // then
        assertEquals(HttpStatus.FORBIDDEN, exception.httpStatus)
    }

    @Test
    fun `patch post not found`() {
        // given
        val userId = 1
        val user =
            User(id = userId, type = "test", identity = "siksha", nickname = "waffle", profileUrl = "https://siksha.wafflestudio.com/")

        val boardId = 2
        val board = Board(id = boardId, name = "test board", description = "test")

        val notExistsPostId = 3

        every { postRepository.findByIdOrNull(notExistsPostId) } returns null

        // when
        val exception =
            assertThrows<PostNotFoundException> {
                service.patchPost(userId, notExistsPostId, PostPatchRequestDto(null, null, null, null))
            }
        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.httpStatus)
    }
}
