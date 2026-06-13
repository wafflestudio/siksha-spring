package siksha.wafflestudio.api.controller.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2AlarmDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2DetailsDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2LikedListResponseDto
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

    @PostMapping("/{menuId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Like V2 menu", description = "Like a normalized V2 menu")
    @SecurityRequirement(name = "bearerAuth")
    fun likeMenu(
        @PathVariable menuId: Long,
        request: HttpServletRequest,
    ): MenuV2DetailsDto = menuService.likeMenu(menuId = menuId, userId = request.userId)

    @PostMapping("/{menuId}/unlike")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Unlike V2 menu", description = "Unlike a normalized V2 menu")
    @SecurityRequirement(name = "bearerAuth")
    fun unlikeMenu(
        @PathVariable menuId: Long,
        request: HttpServletRequest,
    ): MenuV2DetailsDto = menuService.unlikeMenu(menuId = menuId, userId = request.userId)

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get my liked V2 menus", description = "Get V2 menus liked by the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    fun getMyMenus(request: HttpServletRequest): MenuV2LikedListResponseDto = menuService.getMyMenus(userId = request.userId)

    @PostMapping("/{menuId}/alarm/on")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Turn on V2 menu alarm", description = "Turn on alarm for a liked normalized V2 menu")
    @SecurityRequirement(name = "bearerAuth")
    fun menuAlarmOn(
        @PathVariable menuId: Long,
        request: HttpServletRequest,
    ): MenuV2AlarmDto = menuService.menuAlarmOn(menuId = menuId, userId = request.userId)

    @PostMapping("/{menuId}/alarm/off")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Turn off V2 menu alarm", description = "Turn off alarm for a normalized V2 menu")
    @SecurityRequirement(name = "bearerAuth")
    fun menuAlarmOff(
        @PathVariable menuId: Long,
        request: HttpServletRequest,
    ): MenuV2AlarmDto = menuService.menuAlarmOff(menuId = menuId, userId = request.userId)

    @PostMapping("/alarm/off")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Turn off all V2 menu alarms", description = "Turn off all V2 menu alarms for the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    fun menuAlarmOffAll(request: HttpServletRequest) {
        menuService.menuAlarmOffAll(userId = request.userId)
    }

    @PostMapping("/alarm/on")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Turn on all V2 menu alarms", description = "Turn on alarms for all liked V2 menus")
    @SecurityRequirement(name = "bearerAuth")
    fun menuAlarmOnAll(request: HttpServletRequest) {
        menuService.menuAlarmOnAll(userId = request.userId)
    }

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
