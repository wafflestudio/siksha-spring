package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.restaurant.data.Restaurant
import siksha.wafflestudio.core.domain.restaurant.service.RestaurantService

@RestController
class RestaurantController(
    private val restaurantService: RestaurantService,
) {
    @GetMapping("/restaurants")
    fun getRestaurants(request: HttpServletRequest): List<Restaurant> {
        return restaurantService.getRestaurants(request.userId)
    }

    @GetMapping("/restaurants/{restaurantId}")
    fun getRestaurant(request: HttpServletRequest, @PathVariable restaurantId: Long): Restaurant {
        return restaurantService.getRestaurant(restaurantId)
    }
}
