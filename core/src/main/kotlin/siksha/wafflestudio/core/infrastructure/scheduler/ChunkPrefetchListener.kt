package siksha.wafflestudio.core.infrastructure.scheduler

import org.springframework.batch.core.ChunkListener
import org.springframework.batch.core.ItemProcessListener
import org.springframework.batch.core.ItemReadListener
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAlarmV2Repository
import siksha.wafflestudio.core.domain.v1.main.menu.dto.AlarmMenuSummary
import siksha.wafflestudio.core.domain.v1.main.menu.repository.MenuAlarmRepository
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.data.UserDevice
import siksha.wafflestudio.core.domain.user.repository.UserDeviceRepository

@Component
@StepScope
class ChunkPrefetchListener(
    private val userDeviceRepository: UserDeviceRepository,
    private val menuAlarmRepository: MenuAlarmRepository,
    private val menuAlarmV2Repository: MenuAlarmV2Repository,
) : ItemReadListener<User>,
    ItemProcessListener<User, Any>,
    ChunkListener {
    private val currentChunkUsers = mutableListOf<User>()
    var userDevicesMap: Map<Int, List<UserDevice>> = emptyMap()
    var userMenuAlarmsMap: Map<Int, List<AlarmMenuSummary>> = emptyMap()
    private var prefetched = false

    override fun afterRead(item: User) {
        currentChunkUsers.add(item)
    }

    override fun beforeProcess(item: User) {
        if (prefetched) return

        val userIds = currentChunkUsers.map { it.id }

        userDevicesMap =
            userDeviceRepository
                .findAllByUserIds(userIds)
                .groupBy { it.userId.toInt() }

        userMenuAlarmsMap =
            (menuAlarmRepository.findMenuAlarmByUserIds(userIds) +
                menuAlarmV2Repository.findMenuAlarmByUserIds(userIds))
                .groupBy { it.getUserId() }

        prefetched = true
    }

    override fun afterChunk(context: ChunkContext) {
        currentChunkUsers.clear()
        prefetched = false
    }

    override fun onReadError(ex: Exception) {}
}
