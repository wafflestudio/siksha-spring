package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2

interface RestaurantCustomV2Repository : JpaRepository<RestaurantCustomV2, Int> {
    fun findByUserId(userId: Int): RestaurantCustomV2?
}
