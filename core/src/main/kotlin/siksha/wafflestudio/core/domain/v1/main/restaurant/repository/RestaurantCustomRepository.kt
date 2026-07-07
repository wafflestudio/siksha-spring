package siksha.wafflestudio.core.domain.v1.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.v1.main.restaurant.data.RestaurantCustom
import siksha.wafflestudio.core.domain.v1.main.restaurant.data.RestaurantCustomPk

interface RestaurantCustomRepository : JpaRepository<RestaurantCustom, RestaurantCustomPk> {
    fun findRestaurantCustomByUserIdAndRestaurantId(
        userId: Int,
        restaurantId: Int,
    ): RestaurantCustom?

    fun findAllByUserId(userId: Int): List<RestaurantCustom>
}
