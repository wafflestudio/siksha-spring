package siksha.wafflestudio.api.controller

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
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2VisibleRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2VisibleResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.service.RestaurantV2Service

@RestController
@RequestMapping("/v2/restaurants")
@Tag(name = "Restaurants-V2", description = "식당 정보 관리 엔드포인트 V2")
class RestaurantV2Controller(
    private val restaurantService: RestaurantV2Service,
) {
    @GetMapping("")
    @Operation(summary = "식당 목록 조회", description = "모든 식당 목록을 조회합니다")
    fun getRestaurants(): RestaurantV2ListResponseDto = restaurantService.getAllRestaurants()

    @GetMapping("/personal")
    @Operation(summary = "식당 목록 + 즐겨찾기, 순서 정렬에 따른 개인화된 응답 (로그인)", description = "개인화된 식당 목록을 조회합니다 (로그인)")
    fun getPersonalizedRestaurants(request: HttpServletRequest): RestaurantV2ListResponseDto =
        restaurantService.getAllPersonalizedRestaurants(request.userId)

    @PatchMapping("/like/{restaurantId}")
    @Operation(summary = "식당 좋아요 누르기/취소", description = "특정 식당을 좋아요하거나 취소합니다")
    fun setRestaurantLike(
        request: HttpServletRequest,
        @PathVariable restaurantId: Int,
        @RequestBody requestBody: RestaurantV2LikeRequestDto,
    ): RestaurantV2LikeResponseDto = restaurantService.setRestaurantLike(request.userId, restaurantId, requestBody.like)

    @PatchMapping("/visible/{restaurantId}")
    @Operation(summary = "식당 보이기/숨기기", description = "특정 식당을 보이거나 숨깁니다")
    fun setRestaurantVisible(
        request: HttpServletRequest,
        @PathVariable restaurantId: Int,
        @RequestBody requestBody: RestaurantV2VisibleRequestDto,
    ): RestaurantV2VisibleResponseDto = restaurantService.setRestaurantVisible(request.userId, restaurantId, requestBody.visible)

    @GetMapping("/order")
    @Operation(summary = "식당 순서 조회", description = "식당의 순서를 조회합니다")
    fun getRestaurantOrder(request: HttpServletRequest): RestaurantV2OrderResponseDto = restaurantService.getRestaurantOrder(request.userId)

    @PatchMapping("/order")
    @Operation(summary = "식당 순서 변경", description = "식당의 순서를 변경합니다")
    fun changeRestaurantOrder(
        request: HttpServletRequest,
        @RequestBody requestBody: RestaurantV2OrderUpdateRequestDto,
    ): RestaurantV2OrderUpdateResponseDto = restaurantService.changeRestaurantOrder(request.userId, requestBody)
}
