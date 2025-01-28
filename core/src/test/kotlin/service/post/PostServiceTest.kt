package siksha.wafflestudio.core.service.post

import io.mockk.*
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
import siksha.wafflestudio.core.domain.common.exception.BoardNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidPostFormException
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException
import siksha.wafflestudio.core.domain.image.data.Image
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.post.data.Post
import siksha.wafflestudio.core.domain.post.repository.PostLikeRepository
import siksha.wafflestudio.core.domain.post.repository.PostReportRepository
import siksha.wafflestudio.core.domain.post.repository.PostRepository
import siksha.wafflestudio.core.domain.post.service.PostDomainService
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.s3.S3ImagePrefix
import siksha.wafflestudio.core.infrastructure.s3.S3Service
import siksha.wafflestudio.core.infrastructure.s3.UploadFileDto
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
    fun `get posts`() {
        // given
        val userId = 1L
        val boardId = 1L

        val page = 1
        val perPage = 10

        val user = User(
            id = userId,
            nickname = "user",
            type = "test",
            identity = "test"
        )

        val board = Board(id = boardId, name = "test", description = "test")

        val post = Post(
            user = user,
            board = board,
            title = "test",
            content = "test post",
            available = true,
            anonymous = false,
        )

        val pageable = PageRequest.of(page-1, perPage)

        every { boardRepository.existsById(boardId) } returns true
        every { postRepository.findPageByBoardId(boardId, pageable) } returns PageImpl(listOf(post), pageable, 1)
        every { postLikeRepository.findByPostIdIn(any()) } returns emptyList()
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
        verify { postLikeRepository.findByPostIdIn(any()) }
        verify { commentRepository.findByPostIdIn(any()) }

    }

    @Test
    fun `create post invalid title`() {
        // given

        // when
        val dto = PostCreateDto(boardId = 1L, title = " ", content = "test", anonymous = null, images = null)
        val exception = assertThrows<InvalidPostFormException> {
            service.createPost(userId = 1L, postCreateDto = dto)
        }
        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.httpStatus)
        assertEquals("제목은 1자에서 200자 사이여야 합니다.", exception.errorMessage)
    }

    @Test
    fun `create post invalid content`() {
        // given

        // when
        val dto = PostCreateDto(boardId = 1L, title = "test", content = " ", anonymous = null, images = null)
        val exception = assertThrows<InvalidPostFormException> {
            service.createPost(userId = 1L, postCreateDto = dto)
        }
        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.httpStatus)
        assertEquals("내용은 1자에서 1000자 사이여야 합니다.", exception.errorMessage)
    }

    @Test
    fun `create post unauthorized` () {
        // given
        every { userRepository.findByIdOrNull(any()) } returns null
        // when
        val dto = PostCreateDto(boardId = 1L, title = "test", content = "siksha fighting", anonymous = null, images = null)
        val exception = assertThrows<UnauthorizedUserException> {
            service.createPost(userId = 1L, postCreateDto = dto)
        }
        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.httpStatus)
        assertEquals("존재하지 않는 사용자입니다.", exception.errorMessage)
    }

    @Test
    fun `create post invalid board`() {
        // given
        val userId = 1L
        val user = User(id = userId, type = "test", identity = "siksha", nickname = "waffle")

        every { userRepository.findByIdOrNull(userId) } returns user
        every { boardRepository.findByIdOrNull(any()) } returns null
        // when
        val dto = PostCreateDto(boardId = 1L, title = "test", content = "siksha fighting", anonymous = null, images = null)
        val exception = assertThrows<BoardNotFoundException> {
            service.createPost(userId = userId, postCreateDto = dto)
        }

        // then
        verify { userRepository.findByIdOrNull(userId) }
        verify { boardRepository.findByIdOrNull(any()) }
    }

    @Test
    fun `create post anonymous without image`() {
        // given
        val userId = 1L
        val user = User(id = userId, type = "test", identity = "siksha", nickname = "waffle", profileUrl = "https://siksha.wafflestudio.com/")

        val boardId = 2L
        val board = Board(id = boardId, name = "test board", description = "test")

        every { userRepository.findByIdOrNull(userId) } returns user
        every { boardRepository.findByIdOrNull(boardId) } returns board
        every { postRepository.save(any()) } returns mockk()

        // when
        val dto = PostCreateDto(boardId = boardId, title = "test", content = "siksha fighting", anonymous = true, images = null)
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
        val userId = 1L
        val user = User(id = userId, type = "test", identity = "siksha", nickname = "waffle", profileUrl = "https://siksha.wafflestudio.com/")

        val boardId = 2L
        val board = Board(id = boardId, name = "test board", description = "test")

        every { userRepository.findByIdOrNull(userId) } returns user
        every { boardRepository.findByIdOrNull(boardId) } returns board
        every { postRepository.save(any()) } returns mockk()

        // when
        val dto = PostCreateDto(boardId = boardId, title = "test", content = "siksha fighting", anonymous = null, images = null)
        val response = service.createPost(userId, dto)

        // then
        assertEquals("waffle", response.nickname)
        assertEquals("https://siksha.wafflestudio.com/", response.profileUrl)
        assertEquals(true, response.isMine)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { boardRepository.findByIdOrNull(boardId) }
        verify(exactly = 0) { s3Service.uploadFiles(any(), any(), any()) }
    }

    @Test
    fun `create post with images`() {
        // given
        val userId = 1L
        val user = User(id = userId, type = "test", identity = "siksha", nickname = "waffle", profileUrl = "https://siksha.wafflestudio.com/")

        val boardId = 2L
        val board = Board(id = boardId, name = "test board", description = "test")

        val fixedDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0)
        val prefix = S3ImagePrefix.POST

        val nameKey = "board-${boardId}/user-$userId/${fixedDateTime.format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))}"
        val images: List<MultipartFile> = listOf(mockk(), mockk())
        val bucketName = System.getProperty("spring.cloud.aws.s3.bucket")
        val urlPrefix = "https://$bucketName.s3.ap-northeast-2.amazonaws.com/${prefix.prefix}/$nameKey"
        val uploadFileDtos = listOf(
            UploadFileDto(
                key = "${prefix.prefix}/$nameKey/0.jpeg",
                url = "$urlPrefix/0.jpeg"
            ),
            UploadFileDto(
                key = "${prefix.prefix}/$nameKey/1.jpeg",
                url = "$urlPrefix/1.jpeg"
            )
        )

        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fixedDateTime

        every { s3Service.uploadFiles(files = images, prefix = prefix, nameKey = nameKey) } returns uploadFileDtos
        every { imageRepository.saveAll(any<List<Image>>()) } returns mockk()

        every { userRepository.findByIdOrNull(userId) } returns user
        every { boardRepository.findByIdOrNull(boardId) } returns board
        every { postRepository.save(any()) } returns mockk()

        // when
        val dto = PostCreateDto(boardId = boardId, title = "test", content = "siksha fighting", anonymous = false, images = images)
        val response = service.createPost(userId, dto)

        // then
        val parsedEtc = ObjectMapper().readValue(response.etc, Map::class.java)

        assertEquals(mapOf("images" to listOf("$urlPrefix/0.jpeg", "$urlPrefix/1.jpeg")), parsedEtc)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { boardRepository.findByIdOrNull(boardId) }
    }
}
