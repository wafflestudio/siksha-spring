package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2Pk

interface RestaurantCustomV2Repository : JpaRepository<RestaurantCustomV2, RestaurantCustomV2Pk> {
    fun findRestaurantCustomV2ByUserIdAndRestaurantId(
        userId: Int,
        restaurantId: Int,
    ): RestaurantCustomV2?

    fun findAllByUserId(userId: Int): List<RestaurantCustomV2>
}
