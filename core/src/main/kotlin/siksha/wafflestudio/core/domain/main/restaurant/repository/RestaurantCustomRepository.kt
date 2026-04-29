package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustom

interface RestaurantCustomRepository : JpaRepository<RestaurantCustom, Int> {
    fun findRestaurantCustomByUserIdAndRestaurantId(
        userId: Int,
        restaurantId: Int,
    ): RestaurantCustom?

    fun findAllByUserId(userId: Int): List<RestaurantCustom>

    fun findAllByUserIdAndOrderIndexIsNotNullOrderByOrderIndexAsc(userId: Int): List<RestaurantCustom>
}
