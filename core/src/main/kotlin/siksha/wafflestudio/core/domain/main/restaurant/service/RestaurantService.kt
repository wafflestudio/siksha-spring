package siksha.wafflestudio.core.domain.main.menu.restaurant.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.main.menu.restaurant.dto.RestaurantResponseDto
import siksha.wafflestudio.core.domain.main.menu.restaurant.dto.RestaurantListResponseDto
import siksha.wafflestudio.core.domain.main.menu.restaurant.repository.RestaurantRepository

@Service
class RestaurantService(
    private val restaurantRepository: RestaurantRepository,
) {
    @Cacheable(value = ["restaurantCache"])
    fun getAllRestaurants(): RestaurantListResponseDto {
        val restaurants = restaurantRepository.findAll()
        return RestaurantListResponseDto(
            count = restaurants.size,
            result = restaurants.map { restaurant->
                RestaurantResponseDto.from(restaurant)
            }
        )
    }
}
