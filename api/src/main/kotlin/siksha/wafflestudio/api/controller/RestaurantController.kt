package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.service.RestaurantService

@RestController
@Tag(name = "Restaurants", description = "식당 정보 관리 엔드포인트")
class RestaurantController(
    private val restaurantService: RestaurantService,
) {
    @GetMapping("/restaurants")
    @Operation(summary = "식당 목록 조회", description = "모든 식당 목록을 조회합니다")
    fun getRestaurants(): RestaurantListResponseDto {
        return restaurantService.getAllRestaurants()
    }
}
