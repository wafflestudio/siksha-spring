package siksha.wafflestudio.core.domain.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.user.data.UserDevice

@Repository
interface UserDeviceRepository : JpaRepository<UserDevice, Int> {
    fun deleteByFcmToken(fcmToken: String)

    fun deleteByUserIdAndFcmToken(
        userId: Int,
        fcmToken: String,
    )

    @Query(
        """
        SELECT u.*
        FROM user_device u
        where u.user_id in :userIds
    """,
        nativeQuery = true,
    )
    fun findAllByUserIds(userIds: List<Int>): List<UserDevice>
}
