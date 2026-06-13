package siksha.wafflestudio.api.controller.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2DetailsDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2ListResponseDto
import siksha.wafflestudio.core.domain.main.menu.service.MenuV2Service
import java.time.LocalDate

@RestController
@RequestMapping("/v2/menus")
@Tag(name = "Menus-V2", description = "Menu V2 endpoints")
class MenuV2Controller(
    private val menuService: MenuV2Service,
) {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get personalized V2 menus", description = "Get V2 menus by date range with custom order and likes")
    @SecurityRequirement(name = "bearerAuth")
    fun getMenusWhereDate(
        @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam("except_empty", defaultValue = "false") exceptEmpty: Boolean,
        request: HttpServletRequest,
    ): MenuV2ListResponseDto =
        menuService.getMenusWhereDate(
            startDate = startDate,
            endDate = endDate,
            exceptEmpty = exceptEmpty,
            userId = request.userId,
        )

    @GetMapping("/web")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get web V2 menus", description = "Get V2 menus by date range for unauthenticated clients")
    fun getMenusWhereDateWithoutAuth(
        @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam("except_empty", defaultValue = "false") exceptEmpty: Boolean,
    ): MenuV2ListResponseDto =
        menuService.getMenusWhereDate(
            startDate = startDate,
            endDate = endDate,
            exceptEmpty = exceptEmpty,
            userId = null,
        )

    @GetMapping("/{menuId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get V2 menu detail", description = "Get one normalized V2 menu with meal contexts")
    @SecurityRequirement(name = "bearerAuth")
    fun getMenuById(
        @PathVariable menuId: Long,
        request: HttpServletRequest,
    ): MenuV2DetailsDto = menuService.getMenuById(menuId = menuId, userId = request.userId)

    @GetMapping("/{menuId}/web")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get web V2 menu detail", description = "Get one normalized V2 menu for unauthenticated clients")
    fun getMenuByIdWithoutAuth(
        @PathVariable menuId: Long,
    ): MenuV2DetailsDto = menuService.getMenuById(menuId = menuId, userId = null)
}
