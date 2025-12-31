package siksha.wafflestudio.core.infrastructure.scheduler

import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.main.menu.repository.MenuRepository
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.dto.DailyMenuAlarm
import siksha.wafflestudio.core.domain.user.dto.MenuAlarmSendDto
import java.time.LocalDate

@Component
@StepScope
@Qualifier("dailyAlarmProcessor")
class DailyAlarmProcessor(
    private val menuRepository: MenuRepository,
    private val chunkPrefetchListener: ChunkPrefetchListener,
) : ItemProcessor<User, DailyMenuAlarm> {
    private val todayMenusSet: Set<Pair<String, Int>> by lazy {
        menuRepository.findAllByDate(LocalDate.now())
            .map { it.code to it.restaurant.id }
            .toSet()
    }

    override fun process(user: User): DailyMenuAlarm? {
        val devices =
            chunkPrefetchListener.userDevicesMap[user.id].orEmpty()

        if (devices.isEmpty()) return null

        val menus =
            chunkPrefetchListener.userMenuAlarmsMap[user.id].orEmpty()
                .filter { (it.getCode() to it.getRestaurantId()) in todayMenusSet }
                .mapNotNull {
                    it.getNameKr()?.let { nameKr ->
                        MenuAlarmSendDto(
                            menuName = nameKr,
                            restaurantName = it.getRestaurantName(),
                        )
                    }
                }

        if (menus.isEmpty()) return null

        return DailyMenuAlarm(
            userId = user.id,
            alarmType = "DAILY",
            devices = devices,
            menus = menus,
        )
    }
}
