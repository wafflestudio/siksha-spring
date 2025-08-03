package siksha.wafflestudio.api.controller

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
import siksha.wafflestudio.core.domain.main.menu.dto.MenuDetailsDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuListResponseDto
import siksha.wafflestudio.core.domain.main.menu.service.MenuService
import java.time.LocalDate

@RestController
@RequestMapping("/menus")
class MenuController(
    private val menuService: MenuService,
) {
    // GET /menus
    // score, review_cnt, like_cnt, is_liked (if logged in)
    // token 유무를 authInterceptor 내부에서 자동으로 처리
    // except_empty = true, except restaurants with no menus
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
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

    // GET /menus/{menu_id}
    // 특정 menu의 상세 정보 조회
    // token 유무를 authInterceptor 내부에서 자동으로 처리
    @GetMapping("/{menu_id}")
    @ResponseStatus(HttpStatus.OK)
    fun getMenuById(
        @PathVariable("menu_id") menuId: Int,
        request: HttpServletRequest,
    ): MenuDetailsDto {
        return menuService.getMenuById(
            menuId = menuId,
            userId = request.userId,
        )
    }

    // authInterceptor에서 token 유효성 검사 후, userId를 request attribute에 설정
    // 경로를 예외로 설정 안함 -> token 검사 실패 시, exception 발생
    // POST /menus/{menu_id}/like
    @PostMapping("/{menu_id}/like")
    @ResponseStatus(HttpStatus.CREATED)
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
    fun unlikeMenu(
        @PathVariable("menu_id") menuId: Int,
        request: HttpServletRequest,
    ) = menuService.unlikeMenu(
        menuId = menuId,
        userId = request.userId,
    )
}
