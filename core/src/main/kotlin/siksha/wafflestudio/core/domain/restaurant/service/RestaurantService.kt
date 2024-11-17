package siksha.wafflestudio.core.domain.restaurant.service

import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFound
import siksha.wafflestudio.core.domain.restaurant.data.Restaurant
import siksha.wafflestudio.core.domain.restaurant.data.RestaurantRepository
import kotlin.jvm.optionals.getOrNull

@Service
class RestaurantService(
    private val restaurantRepository: RestaurantRepository,
) {
    fun getRestaurants(userId: Long): List<Restaurant> {
        return restaurantRepository.findAll()
    }

    fun getRestaurant(restaurantId: Long): Restaurant {
        return restaurantRepository.findById(restaurantId).getOrNull() ?: throw RestaurantNotFound()
    }
}
