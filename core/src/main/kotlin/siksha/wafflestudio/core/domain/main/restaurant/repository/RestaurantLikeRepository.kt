package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantLike

interface RestaurantLikeRepository : JpaRepository<RestaurantLike, Int> {
    fun findRestaurantLikeByUserIdAndRestaurantId(
        userId: Int,
        restaurantId: Int,
    ): RestaurantLike?
}
