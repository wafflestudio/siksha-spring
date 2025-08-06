package siksha.wafflestudio.core.domain.user.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import siksha.wafflestudio.core.domain.restaurant.data.Restaurant
import siksha.wafflestudio.core.domain.restaurant.dto.RestaurantResponseDto
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.util.EtcUtils
import java.time.OffsetDateTime

// TODO: remove this and use UserWithProfileUrlResponseDto instead
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserResponseDto(
    val id: Int = 0,
    val type: String,
    val identity: String,
    val etc: String? = null,
    val nickname: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun from(user: User): UserResponseDto {
            return UserResponseDto(
                id = user.id,
                type = user.type,
                identity = user.identity,
                etc = user.etc,
                nickname = user.nickname,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserWithProfileUrlResponseDto(
    val id: Int = 0,
    val type: String,
    val identity: String,
    val etc: String? = null,
    val nickname: String,
    val profileUrl: String? = null,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun from(user: User): UserWithProfileUrlResponseDto {
            return UserWithProfileUrlResponseDto(
                id = user.id,
                type = user.type,
                identity = user.identity,
                etc = user.etc,
                nickname = user.nickname,
                profileUrl = user.profileUrl,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}
