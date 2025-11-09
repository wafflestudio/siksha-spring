package siksha.wafflestudio.core.domain.user.dto

import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.user.data.AlarmType

data class UserProfilePatchDto(
    val nickname: String?,
    val image: MultipartFile?,
    // TODO: 변수명 camelCase로 전환 (Jackson의 NamingStrategy 사용 불가)
    val change_to_default_image: Boolean?,
)

data class AlarmSettingRequestDto(
    val type: AlarmType,
)
