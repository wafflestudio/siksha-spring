package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2

interface CustomV2Repository : JpaRepository<CustomV2, Int> {
    fun findByUserId(userId: Int): CustomV2?
}
