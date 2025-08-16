package siksha.wafflestudio.core.domain.auth.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.auth.JwtProvider
import siksha.wafflestudio.core.domain.auth.dto.AuthResponseDto
import siksha.wafflestudio.core.domain.auth.social.SocialTokenVerifier
import siksha.wafflestudio.core.domain.common.exception.SikshaException
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import kotlin.random.Random

@Service
class AuthService(
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val verifiers: Map<String, SocialTokenVerifier>
) {
    fun getAccessTokenByUserId(userId: Int): AuthResponseDto {
        if (!userRepository.existsById(userId)) throw UnauthorizedUserException()

        val token = jwtProvider.generateAccessToken(userId, ACCESS_TOKEN_LIFETIME)
        return AuthResponseDto(accessToken = token)
    }

    fun signIn(providerName: String, token: String): AuthResponseDto {
        val verifier = verifiers[providerName] ?: throw SikshaException(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다.")
        val socialId = verifier.verify(token)

        val user = userRepository.findByTypeAndIdentity(providerName.uppercase(), socialId)
            ?: signUp(providerName.uppercase(), socialId)

        val accessToken = jwtProvider.generateAccessToken(user.id, ACCESS_TOKEN_LIFETIME)
        return AuthResponseDto(accessToken = accessToken)
    }

    private fun signUp(providerName: String, socialId: String): User {
        // TODO: 닉네임 생성기 구현 (siksha-api: nickname_generator.py 참고)
        val nickname = "식샤인${Random.nextInt(10000, 99999)}"
        val user = User(
            type = providerName,
            identity = socialId,
            nickname = nickname
        )
        return userRepository.save(user)
    }

    companion object {
        private const val ACCESS_TOKEN_LIFETIME = 365L
    }
}
