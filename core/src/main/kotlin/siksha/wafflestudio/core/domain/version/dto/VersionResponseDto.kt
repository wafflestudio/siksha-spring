package siksha.wafflestudio.core.domain.version.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import siksha.wafflestudio.core.domain.version.data.AppVersion

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class VersionResponseDto(
    val minimumVersion: String,
) {
    companion object {
        fun from(appVersion: AppVersion): VersionResponseDto =
            VersionResponseDto(
                minimumVersion = appVersion.minimumVersion,
            )
    }
}
