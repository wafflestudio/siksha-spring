package siksha.wafflestudio.api.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.service.RestaurantService

@RestController
class RestaurantController(
    private val restaurantService: RestaurantService,
) {
    @GetMapping("/restaurants")
    fun getRestaurants(): RestaurantListResponseDto {
        return restaurantService.getAllRestaurants()
    }
}
