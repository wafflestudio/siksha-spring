package siksha.wafflestudio.core.domain.v1.version.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class VersionUpdateRequestDto(
    @field:Pattern(
        regexp = "^[0-9]+\\.[0-9]+\\.[0-9]+$",
        message = "minimum_version은 major.minor.patch 형식이어야 합니다.",
    )
    @field:Size(max = 20, message = "minimum_version은 20자 이하여야 합니다.")
    val minimumVersion: String,
)
