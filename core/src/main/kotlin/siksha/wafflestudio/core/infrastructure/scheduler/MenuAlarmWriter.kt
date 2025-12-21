package siksha.wafflestudio.core.infrastructure.scheduler

import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.user.data.UserDevice
import siksha.wafflestudio.core.domain.user.dto.DailyMenuAlarm
import siksha.wafflestudio.core.infrastructure.firebase.FcmPushClient
import siksha.wafflestudio.core.infrastructure.firebase.PushMessage

private const val FCM_TOKEN_BATCH_SIZE = 10

@Component
class MenuAlarmWriter(
    private val fcmPushClient: FcmPushClient,
) : ItemWriter<DailyMenuAlarm> {
    override fun write(items: Chunk<out DailyMenuAlarm>) {
        items.forEach { alarm ->
            sendMenuAlarms(
                alarm.devices,
                alarm.menuNames,
            )
        }
    }

    private fun sendMenuAlarms(
        userDevices: List<UserDevice>,
        menuNames: List<String>,
    ) {
        userDevices.chunked(FCM_TOKEN_BATCH_SIZE).forEach { device ->
            val pushMessage =
                PushMessage(
                    title = "오늘 제공되는 메뉴",
                    body = menuNames.joinToString(", "),
                )

            fcmPushClient.sendPushMessages(
                userDevices = device,
                pushMessage = pushMessage,
            )
        }
    }
}
