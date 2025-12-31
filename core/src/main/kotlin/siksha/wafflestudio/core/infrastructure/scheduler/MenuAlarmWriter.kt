package siksha.wafflestudio.core.infrastructure.scheduler

import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.user.data.UserDevice
import siksha.wafflestudio.core.domain.user.dto.DailyMenuAlarm
import siksha.wafflestudio.core.domain.user.dto.MenuAlarmSendDto
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
                alarm.alarmType,
                alarm.devices,
                alarm.menus,
            )
        }
    }

    private fun sendMenuAlarms(
        type: String,
        userDevices: List<UserDevice>,
        menus: List<MenuAlarmSendDto>,
    ) {
        userDevices.chunked(FCM_TOKEN_BATCH_SIZE).forEach { device ->
            val pushMessage =
                PushMessage(
                    title = getMessageTitle(type),
                    body = getMessageBody(menus),
                )

            fcmPushClient.sendPushMessages(
                userDevices = device,
                pushMessage = pushMessage,
            )
        }
    }

    private fun getMessageTitle(type: String): String =
        when (type) {
            "BR" -> "\uD83D\uDD14띵동! 아침으로 내가 찜한 메뉴가 나왔어요!"
            "LU" -> "\uD83D\uDD14띵동! 점심으로 내가 찜한 메뉴가 나왔어요!"
            "DN" -> "\uD83D\uDD14띵동! 저녁으로 내가 찜한 메뉴가 나왔어요!"
            else -> "\uD83D\uDD14띵동! 내가 찜한 메뉴가 나왔어요!"
        }

    private fun getMessageBody(menus: List<MenuAlarmSendDto>): String {
        if (menus.isEmpty()) {
            return ""
        } else if (menus.size == 1) {
            val menu = menus.single()
            return "오늘은 내가 찜한 ${menu.menuName}\uD83C\uDF7D\uFE0F${subjectParticle(
                menu.menuName,
            )} ${menu.restaurantName}에서 나오는 날이에요. 오늘 메뉴로 어때요?"
        } else if (menus.size == 2) {
            val menu1 = menus[0]
            val menu2 = menus[1]

            return "오늘은 내가 찜한 ${menu1.menuName}\uD83C\uDF7D\uFE0F${conjunctiveParticle(
                menu1.menuName,
            )} ${menu2.menuName}\uD83C\uDF7D\uFE0F${subjectParticle(menu2.menuName)} 나왔어요. 놓치지말고 확인해보세요!"
        } else {
            val menu1 = menus[0]
            val menu2 = menus[1]
            val size = menus.size - 2

            return "오늘은 내가 찜한 ${menu1.menuName}\uD83C\uDF7D\uFE0F${conjunctiveParticle(
                menu1.menuName,
            )} ${menu2.menuName}\uD83C\uDF7D\uFE0F외에도 ${size}가지의 메뉴가 나왔어요. 이 중에 식사 메뉴 정해보기?"
        }
    }

    private fun conjunctiveParticle(word: String): String {
        val lastChar = word.last()
        if (lastChar !in '가'..'힣') return "와"

        return if ((lastChar.code - '가'.code) % 28 != 0) {
            "과"
        } else {
            "와"
        }
    }

    private fun subjectParticle(word: String): String {
        val lastChar = word.last()
        if (lastChar !in '가'..'힣') return "가"

        return if ((lastChar.code - '가'.code) % 28 != 0) {
            "이"
        } else {
            "가"
        }
    }
}
