package siksha.wafflestudio.core.domain.user.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.web.multipart.MultipartFile

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserProfilePatchDto(
    val nickname: String?,
    val image: MultipartFile?,
    val changeToDefaultImage: Boolean,
)
