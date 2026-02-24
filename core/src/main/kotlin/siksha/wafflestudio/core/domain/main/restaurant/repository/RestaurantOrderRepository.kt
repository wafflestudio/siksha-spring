package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantOrder

interface RestaurantOrderRepository : JpaRepository<RestaurantOrder, Int> {
    fun findRestaurantOrderByUserId(userId: Int): RestaurantOrder?
}
