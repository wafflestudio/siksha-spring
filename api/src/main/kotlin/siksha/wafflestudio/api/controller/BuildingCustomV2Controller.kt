package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.service.BuildingCustomV2Service

@RestController
@RequestMapping("/v2/buildingCustoms")
@Tag(name = "Building-Customs-V2", description = "사용자 지정 건물 순서 관리 엔드포인트 V2")
class BuildingCustomV2Controller(
    private val buildingCustomService: BuildingCustomV2Service,
) {
    @GetMapping("/order")
    @Operation(summary = "건물 순서 조회", description = "사용자 지정 건물 순서를 조회합니다")
    fun getBuildingOrder(request: HttpServletRequest): BuildingV2OrderResponseDto = buildingCustomService.getBuildingOrder(request.userId)

    @PatchMapping("/order")
    @Operation(summary = "건물 순서 변경", description = "사용자 지정 건물 순서를 변경합니다")
    fun changeBuildingOrder(
        request: HttpServletRequest,
        @RequestBody requestBody: BuildingV2OrderUpdateRequestDto,
    ): BuildingV2OrderUpdateResponseDto = buildingCustomService.changeBuildingOrder(request.userId, requestBody)
}
