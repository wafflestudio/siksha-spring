package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.main.menu.dto.MenuAlarmRequestDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuDetailsDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuListResponseDto
import siksha.wafflestudio.core.domain.main.menu.dto.MyMenuListResponseDto
import siksha.wafflestudio.core.domain.main.menu.service.MenuService
import java.time.LocalDate

@RestController
@RequestMapping("/menus")
@Tag(name = "Menus", description = "메뉴 관리 엔드포인트")
class MenuController(
    private val menuService: MenuService,
) {
    // GET /menus
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "기간별 메뉴 목록 조회 (로그인)", description = "지정된 기간 동안의 메뉴 목록을 조회합니다 (로그인)")
    @SecurityRequirement(name = "bearerAuth")
    fun getMenusWhereDate(
        @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam("except_empty", defaultValue = "false") exceptEmpty: Boolean,
        request: HttpServletRequest,
    ): MenuListResponseDto {
        return menuService.getMenusWhereDate(
            startDate = startDate,
            endDate = endDate,
            exceptEmpty = exceptEmpty,
            userId = request.userId,
        )
    }

    // GET /menus/web
    @GetMapping("/web")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "기간별 메뉴 목록 조회 (비로그인)", description = "지정된 기간 동안의 메뉴 목록을 조회합니다 (비로그인)")
    fun getMenusWhereDateWithoutAuth(
        @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam("except_empty", defaultValue = "false") exceptEmpty: Boolean,
    ): MenuListResponseDto {
        return menuService.getMenusWhereDate(
            startDate = startDate,
            endDate = endDate,
            exceptEmpty = exceptEmpty,
            userId = null,
        )
    }

    // GET /menus/{menu_id}
    @GetMapping("/{menu_id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "메뉴 상세 조회 (로그인)", description = "특정 메뉴의 상세 정보를 조회합니다 (로그인)")
    @SecurityRequirement(name = "bearerAuth")
    fun getMenuById(
        @PathVariable("menu_id") menuId: Int,
        request: HttpServletRequest,
    ): MenuDetailsDto {
        return menuService.getMenuById(
            menuId = menuId,
            userId = request.userId,
        )
    }

    // GET /menus/{menu_id}/web
    @GetMapping("/{menu_id}/web")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "메뉴 상세 조회 (비로그인)", description = "특정 메뉴의 상세 정보를 조회합니다 (비로그인)")
    fun getMenuByIdWithoutAuth(
        @PathVariable("menu_id") menuId: Int,
    ): MenuDetailsDto {
        return menuService.getMenuById(
            menuId = menuId,
            userId = null,
        )
    }

    // POST /menus/{menu_id}/like
    @PostMapping("/{menu_id}/like")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "메뉴 좋아요", description = "특정 메뉴에 좋아요를 추가합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun likeMenu(
        @PathVariable("menu_id") menuId: Int,
        request: HttpServletRequest,
    ) = menuService.likeMenu(
        menuId = menuId,
        userId = request.userId,
    )

    // POST /menus/{menu_id}/unlike
    @PostMapping("/{menu_id}/unlike")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "메뉴 좋아요 취소", description = "특정 메뉴의 좋아요를 취소합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun unlikeMenu(
        @PathVariable("menu_id") menuId: Int,
        request: HttpServletRequest,
    ) = menuService.unlikeMenu(
        menuId = menuId,
        userId = request.userId,
    )

    // GET /menus/me
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "내가 좋아요한 메뉴 조회", description = "인증된 사용자가 좋아요한 메뉴 목록을 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun getMyMenus(request: HttpServletRequest): MyMenuListResponseDto {
        return menuService.getMyMenus(
            userId = request.userId,
        )
    }

    @PostMapping("/{menu_id}/alarm/on")
    fun menuAlarmOn(
        @PathVariable("menu_id") menuId: Int,
        @RequestBody notificationType: MenuAlarmRequestDto,
        request: HttpServletRequest,
    ) = menuService.menuAlarmOn(
        menuId = menuId,
        userId = request.userId,
    )

    @PostMapping("/{menu_id}/alarm/off")
    fun menuAlarmOff(
        @PathVariable("menu_id") menuId: Int,
        request: HttpServletRequest,
    ) = menuService.menuAlarmOff(
        menuId = menuId,
        userId = request.userId,
    )
}
