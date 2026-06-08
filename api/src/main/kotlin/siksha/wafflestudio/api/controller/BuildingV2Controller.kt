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
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.service.RestaurantV2Service

@RestController
@RequestMapping("/v2/buildings")
@Tag(name = "Buildings-V2", description = "건물 정보 관리 엔드포인트 V2")
class BuildingV2Controller(
    private val restaurantService: RestaurantV2Service,
) {
    @GetMapping("/order")
    @Operation(summary = "건물 순서 조회", description = "사용자 지정 건물 순서를 조회합니다")
    fun getBuildingOrder(request: HttpServletRequest): BuildingV2OrderResponseDto = restaurantService.getBuildingOrder(request.userId)

    @PatchMapping("/order")
    @Operation(summary = "건물 순서 변경", description = "사용자 지정 건물 순서를 변경합니다")
    fun changeBuildingOrder(
        request: HttpServletRequest,
        @RequestBody requestBody: BuildingV2OrderUpdateRequestDto,
    ): BuildingV2OrderUpdateResponseDto = restaurantService.changeBuildingOrder(request.userId, requestBody)

    @GetMapping("/{buildingNumber}/restaurants/order")
    @Operation(summary = "건물 내 식당 순서 조회", description = "특정 건물 내부의 사용자 지정 식당 순서를 조회합니다")
    fun getRestaurantOrderInBuilding(
        request: HttpServletRequest,
        @PathVariable buildingNumber: String,
    ): RestaurantV2OrderResponseDto = restaurantService.getRestaurantOrderInBuilding(request.userId, buildingNumber)

    @PatchMapping("/{buildingNumber}/restaurants/order")
    @Operation(summary = "건물 내 식당 순서 변경", description = "특정 건물 내부의 사용자 지정 식당 순서를 변경합니다")
    fun changeRestaurantOrderInBuilding(
        request: HttpServletRequest,
        @PathVariable buildingNumber: String,
        @RequestBody requestBody: RestaurantV2OrderUpdateRequestDto,
    ): RestaurantV2OrderUpdateResponseDto = restaurantService.changeRestaurantOrderInBuilding(request.userId, buildingNumber, requestBody)
}
