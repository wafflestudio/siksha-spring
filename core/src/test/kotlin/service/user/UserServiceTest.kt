
package siksha.wafflestudio.core.service.user

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.multipart.MultipartFile
import org.testcontainers.junit.jupiter.Testcontainers
import siksha.wafflestudio.core.domain.common.exception.DuplicatedNicknameException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.dto.UserProfilePatchDto
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.domain.user.service.UserService
import siksha.wafflestudio.core.infrastructure.s3.S3ImagePrefix
import siksha.wafflestudio.core.infrastructure.s3.S3Service
import siksha.wafflestudio.core.infrastructure.s3.UploadFileDto
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Testcontainers
@SpringBootTest
class UserServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var imageRepository: ImageRepository
    private lateinit var s3Service: S3Service
    private lateinit var userService: UserService

    @BeforeEach
    internal fun setUp() {
        userRepository = mockk()
        imageRepository = mockk()
        s3Service = mockk()
        userService = UserService(userRepository, imageRepository, s3Service)
        clearAllMocks()
    }

    @Test
    fun `getUser - user not found`() {
        // given
        val notFoundUserId = 999
        every { userRepository.findByIdOrNull(notFoundUserId) } returns null

        // when & then
        assertThrows<UserNotFoundException> {
            userService.getUser(notFoundUserId)
        }

        // verify
        verify { userRepository.findByIdOrNull(notFoundUserId) }
    }

    @Test
    fun `getUser - success`() {
        // given
        val userId = 1
        val user = User(id = userId, nickname = "test-user", type = "test", identity = "test-identity")
        every { userRepository.findByIdOrNull(userId) } returns user

        // when
        val result = userService.getUser(userId)

        // then
        assertEquals(userId, result.id)
        assertEquals("test-user", result.nickname)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
    }

    @Test
    fun `getUserWithProfileUrl - user not found`() {
        // given
        val notFoundUserId = 999
        every { userRepository.findByIdOrNull(notFoundUserId) } returns null

        // when & then
        assertThrows<UserNotFoundException> {
            userService.getUserWithProfileUrl(notFoundUserId)
        }

        // verify
        verify { userRepository.findByIdOrNull(notFoundUserId) }
    }

    @Test
    fun `getUserWithProfileUrl - success`() {
        // given
        val userId = 1
        val profileUrl = "https://example.com/profile.jpg"
        val user = User(id = userId, nickname = "test-user", type = "test", identity = "test-identity", profileUrl = profileUrl)
        every { userRepository.findByIdOrNull(userId) } returns user

        // when
        val result = userService.getUserWithProfileUrl(userId)

        // then
        assertEquals(userId, result.id)
        assertEquals("test-user", result.nickname)
        assertEquals(profileUrl, result.profileUrl)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
    }

    @Test
    fun `deleteUser - user not found`() {
        // given
        val notFoundUserId = 999
        every { userRepository.existsById(notFoundUserId) } returns false

        // when & then
        assertThrows<UserNotFoundException> {
            userService.deleteUser(notFoundUserId)
        }

        // verify
        verify { userRepository.existsById(notFoundUserId) }
    }

    @Test
    fun `deleteUser - success`() {
        // given
        val userId = 1
        every { userRepository.existsById(userId) } returns true
        every { userRepository.deleteById(userId) } just Runs

        // when
        userService.deleteUser(userId)

        // verify
        verify(exactly = 1) { userRepository.existsById(userId) }
        verify(exactly = 1) { userRepository.deleteById(userId) }
    }

    @Test
    fun `patchUserWithProfileUrl - user not found`() {
        // given
        val notFoundUserId = 999
        val request = UserProfilePatchDto(nickname = "new-nickname", image = null, changeToDefaultImage = false)
        every { userRepository.findByIdOrNull(notFoundUserId) } returns null

        // when & then
        assertThrows<UserNotFoundException> {
            userService.patchUserWithProfileUrl(notFoundUserId, request)
        }

        // verify
        verify { userRepository.findByIdOrNull(notFoundUserId) }
    }

    @Test
    fun `patchUserWithProfileUrl - nickname updated`() {
        // given
        val userId = 1
        val user = User(id = userId, nickname = "old-nickname", type = "test", identity = "test-identity")
        val request = UserProfilePatchDto(nickname = "new-nickname", image = null, changeToDefaultImage = false)
        every { userRepository.findByIdOrNull(userId) } returns user
        every { userRepository.existsByNickname("new-nickname") } returns false
        every { userRepository.save(any()) } returns user.apply { nickname = request.nickname!! }

        // when
        val result = userService.patchUserWithProfileUrl(userId, request)

        // then
        assertEquals("new-nickname", result.nickname)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { userRepository.existsByNickname("new-nickname") }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `patchUserWithProfileUrl - duplicated nickname`() {
        // given
        val userId = 1
        val user = User(id = userId, nickname = "old-nickname", type = "test", identity = "test-identity")
        val request = UserProfilePatchDto(nickname = "duplicated-nickname", image = null, changeToDefaultImage = false)
        every { userRepository.findByIdOrNull(userId) } returns user
        every { userRepository.existsByNickname("duplicated-nickname") } returns true

        // when & then
        assertThrows<DuplicatedNicknameException> {
            userService.patchUserWithProfileUrl(userId, request)
        }

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { userRepository.existsByNickname("duplicated-nickname") }
    }

    @Test
    fun `patchUserWithProfileUrl - profile image updated`() {
        // given
        val userId = 1
        val user = User(id = userId, nickname = "test-user", type = "test", identity = "test-identity")
        val image = mockk<MultipartFile>()
        val request = UserProfilePatchDto(nickname = null, image = image, changeToDefaultImage = false)
        val uploadFileDto = UploadFileDto(key = "some-key", url = "some-url")

        every { userRepository.findByIdOrNull(userId) } returns user
        every { s3Service.uploadFile(image, S3ImagePrefix.PROFILE, any()) } returns uploadFileDto
        every { imageRepository.save(any()) } returns mockk()
        every { userRepository.save(any()) } returns user.apply { profileUrl = uploadFileDto.url }

        // when
        val result = userService.patchUserWithProfileUrl(userId, request)

        // then
        assertEquals("some-url", result.profileUrl)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { s3Service.uploadFile(image, S3ImagePrefix.PROFILE, any()) }
        verify { imageRepository.save(any()) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `patchUserWithProfileUrl - change to default image`() {
        // given
        val userId = 1
        val user = User(id = userId, nickname = "test-user", type = "test", identity = "test-identity", profileUrl = "some-url")
        val request = UserProfilePatchDto(nickname = null, image = null, changeToDefaultImage = true)
        val savedUser = user.copy().apply { profileUrl = null }

        every { userRepository.findByIdOrNull(userId) } returns user
        every { s3Service.getKeyFromUrl("some-url") } returns "some-key"
        every { imageRepository.softDeleteByKeyIn(listOf("some-key")) } returns 1
        every { userRepository.save(any()) } returns savedUser

        // when
        val result = userService.patchUserWithProfileUrl(userId, request)

        // then
        assertNull(result.profileUrl)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { s3Service.getKeyFromUrl("some-url") }
        verify { imageRepository.softDeleteByKeyIn(listOf("some-key")) }
        verify { userRepository.save(any()) }
    }
}
