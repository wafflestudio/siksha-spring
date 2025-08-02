
package siksha.wafflestudio.core.service.user

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.testcontainers.junit.jupiter.Testcontainers
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.domain.user.service.UserService
import siksha.wafflestudio.core.infrastructure.s3.S3Service
import kotlin.test.assertEquals

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
        every { userRepository.deleteById(userId) } returns Unit

        // when
        userService.deleteUser(userId)

        // verify
        verify { userRepository.existsById(userId) }
        verify { userRepository.deleteById(userId) }
    }
}
