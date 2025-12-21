package siksha.wafflestudio.core.infrastructure.scheduler

import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAlarmRepository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuRepository
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.dto.DailyMenuAlarm
import siksha.wafflestudio.core.domain.user.repository.UserDeviceRepository
import java.time.LocalDate

@Component
class MenuAlarmProcessor(
    private val menuRepository: MenuRepository,
    private val userDeviceRepository: UserDeviceRepository,
    private val menuAlarmRepository: MenuAlarmRepository
) : ItemProcessor<User, DailyMenuAlarm> {
    private val todayMenusSet: Set<Pair<String, Int>> by lazy {
        menuRepository.findAllByDate(LocalDate.now())
            .map { it.code to it.restaurant.id }
            .toSet()
    }

    override fun process(user: User): DailyMenuAlarm? {
        val devices = userDeviceRepository.findAllByUserIds(listOf(user.id))
        if (devices.isEmpty()) return null

        val menuNames = menuAlarmRepository.findMenuAlarmByUserIds(listOf(user.id))
            .filter { (it.getCode() to it.getRestaurantId()) in todayMenusSet }
            .mapNotNull { it.getNameKr() }

        if (menuNames.isEmpty()) return null

        return DailyMenuAlarm(
            userId = user.id,
            devices = devices,
            menuNames = menuNames
        )
    }
}
