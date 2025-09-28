package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
import siksha.wafflestudio.api.common.filter.userId
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
    // ============================================================================
    // POST Endpoints (Always Require Authentication)
    // ============================================================================

    @PostMapping("/{menu_id}/like")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "메뉴 좋아요", description = "특정 메뉴에 좋아요를 누릅니다")
    @SecurityRequirement(name = "bearerAuth")
    fun likeMenu(
        @PathVariable("menu_id") menuId: Int,
        request: HttpServletRequest,
    ) = menuService.likeMenu(
        menuId = menuId,
        userId = request.userId,
    )

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

    // ============================================================================
    // GET Endpoints (Authentication Required)
    // ============================================================================

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "내가 좋아요한 메뉴 조회", description = "인증된 사용자가 좋아요한 메뉴를 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun getMyMenus(request: HttpServletRequest): MyMenuListResponseDto {
        return menuService.getMyMenus(
            userId = request.userId,
        )
    }

    // ============================================================================
    // GET Endpoints (Conditional Authentication via is_login parameter)
    // ============================================================================

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "날짜 범위별 메뉴 조회",
        description = "지정된 날짜 범위 내의 메뉴를 조회합니다. 'is_login' 파라미터에 따라 인증이 조건부로 적용됩니다.",
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getMenusWhereDate(
        @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam("except_empty", defaultValue = "false") exceptEmpty: Boolean,
        @Parameter(description = "사용자 로그인 여부. 인증 요구사항을 결정합니다.")
        @RequestParam("is_login") isLogin: Boolean,
        request: HttpServletRequest,
    ): MenuListResponseDto {
        return menuService.getMenusWhereDate(
            startDate = startDate,
            endDate = endDate,
            exceptEmpty = exceptEmpty,
            userId = request.userId,
        )
    }

    @GetMapping("/{menu_id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "메뉴 상세 조회",
        description = "특정 메뉴의 상세 정보를 조회합니다. 'is_login' 파라미터에 따라 인증이 조건부로 적용됩니다.",
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getMenuById(
        @PathVariable("menu_id") menuId: Int,
        @Parameter(description = "사용자 로그인 여부. 인증 요구사항을 결정합니다.")
        @RequestParam("is_login") isLogin: Boolean,
        request: HttpServletRequest,
    ): MenuDetailsDto {
        return menuService.getMenuById(
            menuId = menuId,
            userId = request.userId,
        )
    }
}
