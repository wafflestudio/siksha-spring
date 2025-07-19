package siksha.wafflestudio.core.domain.user.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.user.dto.UserResponseDto
import siksha.wafflestudio.core.domain.user.dto.UserWithProfileUrlResponseDto
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getUserInfo(userId: Int): UserResponseDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        return UserResponseDto.from(user)
    }

    fun getUserInfoWithProfileUrl(userId: Int): UserWithProfileUrlResponseDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        return UserWithProfileUrlResponseDto.from(user)
    }
}
