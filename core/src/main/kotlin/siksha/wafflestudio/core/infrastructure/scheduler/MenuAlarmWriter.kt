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
                    title = "\uD83D\uDD14띵동! 내가 찜한 메뉴가 나왔어요!",
                    body = "오늘은 \uD83C\uDF7D\uFE0F${menuNames.joinToString(", ")}\uD83C\uDF7D\uFE0F${subjectParticle(menuNames.last())} 나오는 날이에요.\n알림을 클릭해 새로운 리뷰를 확인해 보세요.",
                )

            fcmPushClient.sendPushMessages(
                userDevices = device,
                pushMessage = pushMessage,
            )
        }
    }

    private fun subjectParticle(word: String): String {
        val lastChar = word.last()

        return if ((lastChar.code - '가'.code) % 28 != 0) "이"
        else "가"
    }
}
