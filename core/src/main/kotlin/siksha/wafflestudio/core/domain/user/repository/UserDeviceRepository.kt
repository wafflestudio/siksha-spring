package siksha.wafflestudio.core.domain.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.user.data.UserDevice

@Repository
interface UserDeviceRepository : JpaRepository<UserDevice, Int> {
    fun deleteByFcmToken(fcmToken: String)

    fun deleteByUserIdAndFcmToken(
        userId: Int,
        fcmToken: String,
    )
}
