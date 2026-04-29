package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantLikeRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantLikeResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantOrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantOrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantOrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantVisibleRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantVisibleResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.service.RestaurantService

@RestController
@Tag(name = "Restaurants", description = "식당 정보 관리 엔드포인트")
class RestaurantController(
    private val restaurantService: RestaurantService,
) {
    @GetMapping("/restaurants")
    @Operation(summary = "식당 목록 조회", description = "모든 식당 목록을 조회합니다")
    fun getRestaurants(): RestaurantListResponseDto = restaurantService.getAllRestaurants()

    @GetMapping("/restaurants/personal")
    @Operation(summary = "식당 목록 + 즐겨찾기, 순서 정렬에 따른 개인화된 응답 (로그인)", description = "개인화된 식당 목록을 조회합니다 (로그인)")
    fun getPersonalizedRestaurants(request: HttpServletRequest): RestaurantListResponseDto =
        restaurantService.getAllPersonalizedRestaurants(request.userId)

    @PostMapping("/restaurants/like/{restaurantId}")
    @Operation(summary = "식당 좋아요 누르기/취소", description = "특정 식당을 좋아요하거나 취소합니다")
    fun setRestaurantLike(
        request: HttpServletRequest,
        @PathVariable restaurantId: Int,
        @RequestBody requestBody: RestaurantLikeRequestDto,
    ): RestaurantLikeResponseDto = restaurantService.setRestaurantLike(request.userId, restaurantId, requestBody.like)

    @PostMapping("/restaurants/visible/{restaurantId}")
    @Operation(summary = "식당 보이기/숨기기", description = "특정 식당을 보이거나 숨깁니다")
    fun setRestaurantVisible(
        request: HttpServletRequest,
        @PathVariable restaurantId: Int,
        @RequestBody requestBody: RestaurantVisibleRequestDto,
    ): RestaurantVisibleResponseDto = restaurantService.setRestaurantVisible(request.userId, restaurantId, requestBody.visible)

    @GetMapping("/restaurants/order")
    @Operation(summary = "식당 순서 조회", description = "식당의 순서를 조회합니다")
    fun getRestaurantOrder(request: HttpServletRequest): RestaurantOrderResponseDto = restaurantService.getRestaurantOrder(request.userId)

    @PatchMapping("/restaurants/order")
    @Operation(summary = "식당 순서 변경", description = "식당의 순서를 변경합니다")
    fun changeRestaurantOrder(
        request: HttpServletRequest,
        @RequestBody requestBody: RestaurantOrderUpdateRequestDto,
    ): RestaurantOrderUpdateResponseDto = restaurantService.changeRestaurantOrder(request.userId, requestBody)
}
