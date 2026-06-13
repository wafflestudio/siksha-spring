package siksha.wafflestudio.api.controller.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2LikeRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2LikeResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2VisibleRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2VisibleResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.service.RestaurantV2Service

@RestController
@RequestMapping("/v2/restaurants")
@Tag(name = "Restaurants-V2", description = "Restaurant V2 endpoints")
class RestaurantV2Controller(
    private val restaurantService: RestaurantV2Service,
) {
    @GetMapping("")
    @Operation(summary = "Get restaurants", description = "Get all restaurants grouped by building")
    fun getRestaurants(): RestaurantV2ListResponseDto = restaurantService.getAllRestaurants()

    @GetMapping("/personal")
    @Operation(summary = "Get personalized restaurants", description = "Get restaurants with building custom, restaurant custom, and likes")
    fun getPersonalizedRestaurants(request: HttpServletRequest): RestaurantV2ListResponseDto =
        restaurantService.getAllPersonalizedRestaurants(request.userId)

    @PatchMapping("/like/{restaurantId}")
    @Operation(summary = "Set restaurant like", description = "Like or unlike a restaurant")
    fun setRestaurantLike(
        request: HttpServletRequest,
        @PathVariable restaurantId: Int,
        @RequestBody requestBody: RestaurantV2LikeRequestDto,
    ): RestaurantV2LikeResponseDto = restaurantService.setRestaurantLike(request.userId, restaurantId, requestBody.like)

    @PatchMapping("/visible/{restaurantId}")
    @Operation(summary = "Set restaurant visibility", description = "Show or hide a restaurant")
    fun setRestaurantVisible(
        request: HttpServletRequest,
        @PathVariable restaurantId: Int,
        @RequestBody requestBody: RestaurantV2VisibleRequestDto,
    ): RestaurantV2VisibleResponseDto = restaurantService.setRestaurantVisible(request.userId, restaurantId, requestBody.visible)
}
