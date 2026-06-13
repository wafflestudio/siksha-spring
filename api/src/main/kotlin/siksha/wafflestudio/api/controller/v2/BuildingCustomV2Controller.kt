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
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2VisibleRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2VisibleResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.service.BuildingCustomV2Service

@RestController
@RequestMapping("/v2/buildingCustoms")
@Tag(name = "Building-Customs-V2", description = "Building custom V2 endpoints")
class BuildingCustomV2Controller(
    private val buildingCustomService: BuildingCustomV2Service,
) {
    @GetMapping("/order")
    @Operation(summary = "Get building order", description = "Get effective building order")
    fun getBuildingOrder(request: HttpServletRequest): BuildingV2OrderResponseDto =
        buildingCustomService.getBuildingOrder(request.userId)

    @PatchMapping("/order")
    @Operation(summary = "Change building order", description = "Change custom building order")
    fun changeBuildingOrder(
        request: HttpServletRequest,
        @RequestBody requestBody: BuildingV2OrderUpdateRequestDto,
    ): BuildingV2OrderUpdateResponseDto = buildingCustomService.changeBuildingOrder(request.userId, requestBody)

    @PatchMapping("/visible/{buildingNumber}")
    @Operation(summary = "Set building visibility", description = "Show or hide a building")
    fun setBuildingVisible(
        request: HttpServletRequest,
        @PathVariable buildingNumber: String,
        @RequestBody requestBody: BuildingV2VisibleRequestDto,
    ): BuildingV2VisibleResponseDto = buildingCustomService.setBuildingVisible(request.userId, buildingNumber, requestBody)

    @GetMapping("/{buildingNumber}/restaurants/order")
    @Operation(summary = "Get restaurant order in building", description = "Get effective restaurant order inside a building")
    fun getRestaurantOrder(
        request: HttpServletRequest,
        @PathVariable buildingNumber: String,
    ): RestaurantV2OrderResponseDto = buildingCustomService.getRestaurantOrder(request.userId, buildingNumber)

    @PatchMapping("/{buildingNumber}/restaurants/order")
    @Operation(summary = "Change restaurant order in building", description = "Change custom restaurant order inside a building")
    fun changeRestaurantOrder(
        request: HttpServletRequest,
        @PathVariable buildingNumber: String,
        @RequestBody requestBody: RestaurantV2OrderUpdateRequestDto,
    ): RestaurantV2OrderUpdateResponseDto = buildingCustomService.changeRestaurantOrder(request.userId, buildingNumber, requestBody)
}
