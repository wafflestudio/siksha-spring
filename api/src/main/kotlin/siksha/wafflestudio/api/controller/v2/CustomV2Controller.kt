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
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2CustomResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2CustomUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2CustomUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2CustomResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2CustomUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2CustomUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.service.BuildingCustomV2Service

@RestController
@RequestMapping("/v2/customs")
@Tag(name = "Customs-V2", description = "Custom V2 endpoints")
class CustomV2Controller(
    private val buildingCustomService: BuildingCustomV2Service,
) {
    @GetMapping("/buildings")
    @Operation(summary = "Get building customs", description = "Get effective building custom snapshot")
    fun getBuildingCustoms(request: HttpServletRequest): BuildingV2CustomResponseDto =
        buildingCustomService.getBuildingCustoms(request.userId)

    @PatchMapping("/buildings")
    @Operation(summary = "Update building customs", description = "Update the full building custom snapshot")
    fun updateBuildingCustoms(
        request: HttpServletRequest,
        @RequestBody requestBody: BuildingV2CustomUpdateRequestDto,
    ): BuildingV2CustomUpdateResponseDto = buildingCustomService.updateBuildingCustoms(request.userId, requestBody)

    @GetMapping("/buildings/{buildingNumber}/restaurants")
    @Operation(summary = "Get restaurant customs in building", description = "Get effective restaurant custom snapshot inside a building")
    fun getRestaurantCustoms(
        request: HttpServletRequest,
        @PathVariable buildingNumber: String,
    ): RestaurantV2CustomResponseDto = buildingCustomService.getRestaurantCustoms(request.userId, buildingNumber)

    @PatchMapping("/buildings/{buildingNumber}/restaurants")
    @Operation(
        summary = "Update restaurant customs in building",
        description = "Update the full restaurant custom snapshot inside a building",
    )
    fun updateRestaurantCustoms(
        request: HttpServletRequest,
        @PathVariable buildingNumber: String,
        @RequestBody requestBody: RestaurantV2CustomUpdateRequestDto,
    ): RestaurantV2CustomUpdateResponseDto = buildingCustomService.updateRestaurantCustoms(request.userId, buildingNumber, requestBody)
}
