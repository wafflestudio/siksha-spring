package siksha.wafflestudio.core.domain.user.dto

import siksha.wafflestudio.core.domain.user.data.UserDevice

data class UserDeviceDto(
    val fcmToken: String,
)

data class MenuAlarmSendDto(
    val menuName: String,
    val restaurantName: String,
)

data class DailyMenuAlarm(
    val userId: Int,
    val alarmType: String,
    val devices: List<UserDevice>,
    val menus: List<MenuAlarmSendDto>,
)
