package siksha.wafflestudio.api.controller.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.main.restaurant.dto.CustomV2ResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.CustomV2UpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.CustomV2UpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.service.CustomV2Service

@RestController
@RequestMapping("/v2/customs")
@Tag(name = "Customs-V2", description = "Custom V2 endpoints")
class CustomV2Controller(
    private val customService: CustomV2Service,
) {
    @GetMapping
    @Operation(summary = "Get customs", description = "Get effective building and restaurant custom snapshot")
    fun getCustoms(request: HttpServletRequest): CustomV2ResponseDto = customService.getCustoms(request.userId)

    @PatchMapping
    @Operation(summary = "Update customs", description = "Update the full building and restaurant custom snapshot")
    fun updateCustoms(
        request: HttpServletRequest,
        @RequestBody requestBody: CustomV2UpdateRequestDto,
    ): CustomV2UpdateResponseDto = customService.updateCustoms(request.userId, requestBody)
}
