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
    fun upsertAndGetUserId(p: SocialProfile): Int {
        // 1) 이미 매핑되어 있으면 바로 반환 (idempotent)
        userRepository.findByTypeAndIdentity(p.provider.toString(), p.externalId)?.let { return it.id }

        // 2) 없으면 유저 생성 후 매핑 시도
        // TODO: implement nickname generator
        val newUser = User(type = p.provider.toString(), identity = p.externalId, nickname = "dummy")

        return try {
            userRepository.save(newUser)
            newUser.id
        } catch (e: DataIntegrityViolationException) {
            // 3) 동시성(레이스)으로 인해 유니크 충돌 시, 다시 조회해서 반환
            userRepository.findByTypeAndIdentity(p.provider.toString(), p.externalId)?.id
                ?: throw e
        }
    }

    companion object {
        private const val ACCESS_TOKEN_LIFETIME = 365L
    }
}
