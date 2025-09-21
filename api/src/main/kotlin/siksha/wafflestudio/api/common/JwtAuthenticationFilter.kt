package siksha.wafflestudio.api.common

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import siksha.wafflestudio.core.domain.auth.JwtProvider
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {
    private val menuGetPattern = Regex("^/menus(?:/\\d+)?$") // /menus, /menus/{id}

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // 1) 예외 없이 통과하는 케이스
        if (request.method.equals(HttpMethod.OPTIONS.name(), ignoreCase = true)) {
            filterChain.doFilter(request, response)
            return
        }
        if (request.method.equals(HttpMethod.GET.name(), ignoreCase = true) &&
            request.requestURI == "/community/boards"
        ) {
            filterChain.doFilter(request, response)
            return
        }

        val token = extractToken(request)

        // 2) /reviews 공개 열람(익명 userId=0)
        if (request.method.equals(HttpMethod.GET.name(), ignoreCase = true)) {
            val isReviewsUri = request.requestURI.startsWith("/reviews")
            val isMyReviewUri = request.requestURI == "/reviews/me"
            val isPrivate = request.getParameter("is_private")?.toBoolean() ?: false

            if (isReviewsUri && !isMyReviewUri && !isPrivate) {
                // 익명 열람 허용
                setAnonymousPrincipal()
                filterChain.doFilter(request, response)
                return
            }
        }

        // 3) 메뉴 API 특례
        if (request.method.equals(HttpMethod.GET.name(), ignoreCase = true) &&
            menuGetPattern.matches(request.requestURI)
        ) {
            if (token.isNullOrBlank()) {
                // 토큰 없이 익명 접근 허용
                setAnonymousPrincipal()
                filterChain.doFilter(request, response)
                return
            } else {
                // 토큰 있으면 검증 필요
                try {
                    val userId = verifyAndExtractUserId(token)
                    setAuthenticatedPrincipal(userId)
                    filterChain.doFilter(request, response)
                    return
                } catch (e: Exception) {
                    SecurityContextHolder.clearContext()
                    throw UnauthorizedUserException()
                }
            }
        }

        // 4) 그 외 모든 요청: 토큰 필수
        if (token.isNullOrBlank()) {
            SecurityContextHolder.clearContext()
            throw UnauthorizedUserException()
        } else {
            try {
                val userId = verifyAndExtractUserId(token)
                setAuthenticatedPrincipal(userId)
            } catch (e: Exception) {
                SecurityContextHolder.clearContext()
                throw UnauthorizedUserException()
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun verifyAndExtractUserId(token: String): Int {
        val claims = jwtProvider.verifyJwtGetClaims(token)
        val value =
            claims[USER_ID_KEY_IN_JWT]
                ?: throw UnauthorizedUserException()
        // JWT 라이브러리/직렬화 설정에 따라 Int가 Long/Double로 넘어올 수 있어 안전 변환 처리
        return when (value) {
            is Int -> value
            is Long -> value.toInt()
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: throw UnauthorizedUserException()
            else -> throw UnauthorizedUserException()
        }
    }

    private fun setAuthenticatedPrincipal(userId: Int) {
        val authentication =
            UsernamePasswordAuthenticationToken(
                UserPrincipal(userId),
                null,
                emptyList(),
            )
        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun setAnonymousPrincipal() {
        val authentication =
            UsernamePasswordAuthenticationToken(
                UserPrincipal(ANONYMOUS_USER_ID),
                null,
                emptyList(),
            )
        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun extractToken(request: HttpServletRequest): String? {
        // 우선순위: Authorization → Authorization-Token
        val raw =
            request.getHeader("Authorization")
                ?.takeIf { it.isNotBlank() }
                ?: request.getHeader(AUTHORIZATION_HEADER_NAME)?.takeIf { it.isNotBlank() }
        return raw?.removePrefix("Bearer ")?.trim()
    }

    companion object {
        private const val USER_ID_KEY_IN_JWT = "userId"
        private const val AUTHORIZATION_HEADER_NAME = "Authorization-Token"
        private const val ANONYMOUS_USER_ID = 0
    }
}

/**
 * 컨트롤러/서비스 단에서 userId 꺼낼 때 사용.
 * - 인증/익명 모두 UserPrincipal이 주입되므로 그대로 사용 가능.
 * - SecurityContext에 아무 principal도 없다면 UnauthorizedUserException 던짐(정책 위반).
 */
val HttpServletRequest.userId: Int
    get() {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        val userId = (principal as? UserPrincipal)?.userId
        return userId ?: throw UnauthorizedUserException()
    }

data class UserPrincipal(
    val userId: Int,
)
