package siksha.wafflestudio.core.domain.auth.service

import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.auth.JwtProvider
import siksha.wafflestudio.core.domain.auth.dto.AuthResponseDto
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class AuthService(
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
) {
    fun getAccessTokenByUserId(userId: Int): AuthResponseDto {
        if (!userRepository.existsById(userId)) throw UnauthorizedUserException()

        val token = jwtProvider.generateAccessToken(userId, ACCESS_TOKEN_LIFETIME)
        return AuthResponseDto(accessToken = token)
    }

    companion object {
        private const val ACCESS_TOKEN_LIFETIME = 365L
    }
}
