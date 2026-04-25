
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
import org.springframework.data.repository.findByIdOrNull
import siksha.wafflestudio.core.domain.common.exception.DuplicatedNicknameException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.dto.UserProfilePatchDto
import siksha.wafflestudio.core.domain.user.repository.UserDeviceRepository
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.domain.user.service.UserService
import siksha.wafflestudio.core.infrastructure.imageupload.ImageUploadUseCase
import kotlin.test.assertEquals

class UserServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var imageRepository: ImageRepository
    private lateinit var userDeviceRepository: UserDeviceRepository
    private lateinit var imageUploadUseCase: ImageUploadUseCase
    private lateinit var userService: UserService

    @BeforeEach
    internal fun setUp() {
        userRepository = mockk()
        imageRepository = mockk()
        userDeviceRepository = mockk()
        imageUploadUseCase = mockk()
        userService = UserService(userRepository, imageRepository, userDeviceRepository, imageUploadUseCase, listOf())
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
    fun `patchUser - user not found`() {
        // given
        val notFoundUserId = 999
        val request = UserProfilePatchDto(nickname = "new-nickname", image = null, change_to_default_image = false)
        every { userRepository.findByIdOrNull(notFoundUserId) } returns null

        // when & then
        assertThrows<UserNotFoundException> {
            userService.patchUser(notFoundUserId, request)
        }

        // verify
        verify { userRepository.findByIdOrNull(notFoundUserId) }
    }

    @Test
    fun `patchUser - nickname updated`() {
        // given
        val userId = 1
        val user = User(id = userId, nickname = "old-nickname", type = "test", identity = "test-identity")
        val updatedUser = user.copy(nickname = "new-nickname")
        val request = UserProfilePatchDto(nickname = "new-nickname", image = null, change_to_default_image = false)
        every { userRepository.findByIdOrNull(userId) } returns user
        every { userRepository.existsByNickname("new-nickname") } returns false
        every { userRepository.save(any()) } returns updatedUser

        // when
        val result = userService.patchUser(userId, request)

        // then
        assertEquals("new-nickname", result.nickname)

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { userRepository.existsByNickname("new-nickname") }
        verify { userRepository.save(match { it.nickname == "new-nickname" }) }
    }

    @Test
    fun `patchUser - duplicated nickname`() {
        // given
        val userId = 1
        val user = User(id = userId, nickname = "old-nickname", type = "test", identity = "test-identity")
        val request = UserProfilePatchDto(nickname = "duplicated-nickname", image = null, change_to_default_image = false)
        every { userRepository.findByIdOrNull(userId) } returns user
        every { userRepository.existsByNickname("duplicated-nickname") } returns true

        // when & then
        assertThrows<DuplicatedNicknameException> {
            userService.patchUser(userId, request)
        }

        // verify
        verify { userRepository.findByIdOrNull(userId) }
        verify { userRepository.existsByNickname("duplicated-nickname") }
    }

    @Test
    fun `createUserDevice - success`() {
        // given
        val userId = 1
        val fcmToken = "test-fcm-token"
        every { userRepository.existsById(userId) } returns true
        every { userDeviceRepository.deleteByFcmToken(fcmToken) } just Runs
        every { userDeviceRepository.flush() } just Runs
        every { userDeviceRepository.save(any()) } returns mockk()

        // when
        userService.createUserDevice(userId, fcmToken)

        // verify
        verify(exactly = 1) { userRepository.existsById(userId) }
        verify(exactly = 1) { userDeviceRepository.deleteByFcmToken(fcmToken) }
        verify(exactly = 1) { userDeviceRepository.flush() }
        verify(exactly = 1) { userDeviceRepository.save(match { it.userId == userId.toLong() && it.fcmToken == fcmToken }) }
    }

    @Test
    fun `deleteUserDevice - success`() {
        // given
        val userId = 1
        val fcmToken = "test-fcm-token"
        every { userRepository.existsById(userId) } returns true
        every { userDeviceRepository.deleteByUserIdAndFcmToken(userId, fcmToken) } just Runs

        // when
        userService.deleteUserDevice(userId, fcmToken)

        // verify
        verify(exactly = 1) { userRepository.existsById(userId) }
        verify(exactly = 1) { userDeviceRepository.deleteByUserIdAndFcmToken(userId, fcmToken) }
    }
}
