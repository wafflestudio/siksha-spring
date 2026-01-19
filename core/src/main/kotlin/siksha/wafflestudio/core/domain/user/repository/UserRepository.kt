package siksha.wafflestudio.core.domain.user.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.user.data.AlarmType
import siksha.wafflestudio.core.domain.user.data.User

@Repository
interface UserRepository : JpaRepository<User, Int> {
    fun existsByNickname(nickname: String): Boolean

    fun findByTypeAndIdentity(
        type: String,
        identity: String,
    ): User?

    fun findAllByAlarmType(
        alarmType: AlarmType,
        pageable: Pageable,
    ): Page<User>
}
