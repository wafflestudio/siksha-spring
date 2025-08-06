package siksha.wafflestudio.api.common

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import siksha.wafflestudio.core.domain.auth.JwtProvider
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException

@Component
class AuthInterceptor(
    private val jwtProvider: JwtProvider,
) : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val menuGetPattern = Regex("^/menus(?:/\\d+)?$") // /menus 또는 /menus/{menu_id}

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (request.method == HttpMethod.GET.name() && request.requestURI == "/community/boards") return true
        if (request.method == HttpMethod.OPTIONS.name()) return true

        // /menus/와 /menus/lo api를 /menus로 통합하기 위한 처리
        val authHeader = request.getHeader(AUTHORIZATION_HEADER_NAME)
        if (request.method == HttpMethod.GET.name() && menuGetPattern.matches(request.requestURI)) {
            // 로그인하지 않은 상태로 /menus, /menus/{menu_id} 요청 시, userId를 0으로 설정
            // exception 발생 하지 않음
            if (authHeader.isNullOrBlank()) {
                request.setAttribute("userId", 0)
                return true
            }
        }

        // 로그인 상태에서 /menus, /menus/{menu_id} 요청 시, userId를 request attribute에 설정
        // 해당 과정에서 token이 잘못된 경우, UnauthorizedUserException 발생
        // 다른 api에 대해서도 정상적으로 token 검증 작업
        return runCatching {
            val userId = jwtProvider.verifyJwtGetClaims(authHeader)[USER_ID_KEY_IN_JWT]!! as Int
            request.setAttribute("userId", userId)
            true
        }.getOrElse {
            logger.error("auth failed")
            throw UnauthorizedUserException()
        }
    }

    companion object {
        private const val USER_ID_KEY_IN_JWT = "userId"
        private const val AUTHORIZATION_HEADER_NAME = "Authorization-Token"
    }
}

val HttpServletRequest.userId: Int
    get() = this.getAttribute("userId") as Int
