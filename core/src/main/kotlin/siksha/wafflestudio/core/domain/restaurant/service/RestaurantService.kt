package siksha.wafflestudio.core.domain.restaurant.service

import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.restaurant.data.Restaurant
import siksha.wafflestudio.core.domain.restaurant.data.RestaurantRepository

@Service
class RestaurantService(
    private val restaurantRepository: RestaurantRepository,
) {
    fun getRestaurants(userId: Long): List<Restaurant> {
        return restaurantRepository.findAll()
    }
}
