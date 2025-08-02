
package siksha.wafflestudio.core.service.auth

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import siksha.wafflestudio.core.domain.auth.JwtProvider
import siksha.wafflestudio.core.domain.auth.service.AuthService
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import kotlin.test.assertEquals

@Testcontainers
@SpringBootTest
class AuthServiceTest {
    private lateinit var jwtProvider: JwtProvider
    private lateinit var userRepository: UserRepository
    private lateinit var authService: AuthService

    @BeforeEach
    internal fun setUp() {
        jwtProvider = mockk()
        userRepository = mockk()
        authService = AuthService(jwtProvider, userRepository)
        clearAllMocks()
    }

    @Test
    fun `getAccessTokenByUserId - user not found`() {
        // given
        val invalidUserId = 999
        every { userRepository.existsById(invalidUserId) } returns false

        // when & then
        assertThrows<UnauthorizedUserException> {
            authService.getAccessTokenByUserId(invalidUserId)
        }

        // verify
        verify { userRepository.existsById(invalidUserId) }
    }

    @Test
    fun `getAccessTokenByUserId - success`() {
        // given
        val validUserId = 1
        val generatedToken = "test-access-token"

        every { userRepository.existsById(validUserId) } returns true
        every { jwtProvider.generateAccessToken(validUserId, any()) } returns generatedToken

        // when
        val result = authService.getAccessTokenByUserId(validUserId)

        // then
        assertEquals(generatedToken, result.accessToken)

        // verify
        verify { userRepository.existsById(validUserId) }
        verify { jwtProvider.generateAccessToken(validUserId, any()) }
    }
}
