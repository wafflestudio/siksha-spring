package siksha.wafflestudio.core.infrastructure.scheduler

import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAlarmV2Repository
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.dto.DailyMenuAlarm
import siksha.wafflestudio.core.domain.user.dto.MenuAlarmSendDto
import siksha.wafflestudio.core.domain.v1.main.menu.repository.MenuRepository
import java.time.LocalDate

@Component
@StepScope
@Qualifier("dailyAlarmProcessor")
class DailyAlarmProcessor(
    private val menuRepository: MenuRepository,
    private val menuAlarmV2Repository: MenuAlarmV2Repository,
    private val chunkPrefetchListener: ChunkPrefetchListener,
) : ItemProcessor<User, DailyMenuAlarm> {
    private val todayMenusSet: Set<Pair<String, Int>> by lazy {
        (
            menuRepository
                .findAllByDate(LocalDate.now())
                .map { it.getCode() to it.getRestaurantId() } +
                menuAlarmV2Repository
                    .findAllAlarmMenusByDate(LocalDate.now())
                    .map { it.getCode() to it.getRestaurantId() }
        ).toSet()
    }

    override fun process(user: User): DailyMenuAlarm? {
        val devices =
            chunkPrefetchListener.userDevicesMap[user.id].orEmpty()

        if (devices.isEmpty()) return null

        val menus =
            chunkPrefetchListener.userMenuAlarmsMap[user.id]
                .orEmpty()
                .filter { (it.getCode() to it.getRestaurantId()) in todayMenusSet }
                .mapNotNull {
                    it.getNameKr()?.let { nameKr ->
                        MenuAlarmSendDto(
                            menuName = nameKr,
                            restaurantName = it.getRestaurantName(),
                        )
                    }
                }.sortedBy { it.restaurantName }

        if (menus.isEmpty()) return null

        return DailyMenuAlarm(
            userId = user.id,
            alarmType = "DAILY",
            devices = devices,
            menus = menus,
        )
    }
}
