package siksha.wafflestudio.core.infrastructure.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.main.menu.service.MenuService

@Component
class AlarmScheduler(
    private val menuService: MenuService
) {
    @Scheduled(cron = "0 30 7 * * *")
    fun morningAlarm() {
        menuService.sendDailyMenuAlarmsBatch()
    }
}
