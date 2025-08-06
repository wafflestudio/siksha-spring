package siksha.wafflestudio.core.domain.auth.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AuthResponseDto(
    val accessToken: String,
)

// dummy class to pass linter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AuthDummy(
    val dummy: String,
)
