package siksha.wafflestudio.core.domain.user.dto

import siksha.wafflestudio.core.domain.user.data.UserDevice

data class UserDeviceDto(
    val fcmToken: String,
)

data class DailyMenuAlarm(
    val userId: Int,
    val devices: List<UserDevice>,
    val menuNames: List<String>,
)
