package siksha.wafflestudio.core.domain.auth.service

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import siksha.wafflestudio.core.domain.auth.JwtProvider
import siksha.wafflestudio.core.domain.auth.dto.AuthResponseDto
import siksha.wafflestudio.core.domain.auth.social.data.SocialProfile
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository

/**
 * 인증, 인가, 로그인, 회원가입 등의 흐름을 다루는 서비스입니다.
 * User 객체의 CRUD는 UserService의 책임입니다.
 */
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


    @Transactional
    fun upsertUserAndGetAccessToken(p: SocialProfile): AuthResponseDto {
        val existing = userRepository.findByTypeAndIdentity(p.provider.toString(), p.externalId)
        val userId = if (existing != null) {
            existing.id
        } else {
            val newUser = User(
                type = p.provider.toString(),
                identity = p.externalId,
                nickname = "dummy" // TODO: nickname generator
            )

            try {
                userRepository.save(newUser)
                newUser.id
            } catch (e: DataIntegrityViolationException) {
                // race condition
                userRepository.findByTypeAndIdentity(p.provider.toString(), p.externalId)?.id
                    ?: throw e // 500; 유저 생성에 실패했지만 조회도 되지 않는 경우
            }
        }
        return getAccessTokenByUserId(userId)
    }

    companion object {
        private const val ACCESS_TOKEN_LIFETIME = 365L
    }
}
