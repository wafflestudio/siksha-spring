package siksha.wafflestudio.api.common

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import siksha.wafflestudio.core.domain.auth.JwtProvider
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {
    private val menuGetPattern = Regex("^/menus(?:/\\d+)?$") // /menus, /menus/{id}

    // SecurityConfig의 permitAll 목록과 최대한 맞춰주세요
    private val permitAllMatchers =
        listOf(
            AntPathRequestMatcher("/error"),
            AntPathRequestMatcher("/swagger-ui/**"),
            AntPathRequestMatcher("/v3/api-docs/**"),
            AntPathRequestMatcher("/actuator/health"),
            AntPathRequestMatcher("/restaurants"),
            AntPathRequestMatcher("/auth/privacy-policy"),
            AntPathRequestMatcher("/auth/login/**"),
            AntPathRequestMatcher("/auth/nicknames/validate"),
            AntPathRequestMatcher("/docs"),
            // 별도: GET /community/boards (아래에서 따로 처리도 하지만 여기에도 포함)
            AntPathRequestMatcher("/community/boards", "GET"),
        )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // 0) CORS preflight는 무조건 통과
        if (request.method.equals(HttpMethod.OPTIONS.name(), ignoreCase = true)) {
            filterChain.doFilter(request, response)
            return
        }

        // 1) SecurityConfig에서 permitAll로 둔 경로라면:
        //    - 토큰 있으면 검증 후 auth 세팅
        //    - 토큰 없거나/깨져도 예외 던지지 말고 그냥 통과
        if (isPermitAll(request)) {
            val token = extractToken(request)
            if (!token.isNullOrBlank()) {
                runCatching {
                    val userId = verifyAndExtractUserId(token)
                    setAuthenticatedPrincipal(userId)
                }.onFailure {
                    SecurityContextHolder.clearContext() // 그냥 익명으로 통과
                }
            }
            filterChain.doFilter(request, response)
            return
        }

        // 2) GET /community/boards (명시적 허용) — 위 permitAll에도 넣었지만 안전하게 한 번 더
        if (request.method.equals(HttpMethod.GET.name(), ignoreCase = true) &&
            request.requestURI == "/community/boards"
        ) {
            filterChain.doFilter(request, response)
            return
        }

        val token = extractToken(request)

        // 3) /reviews 공개 열람(익명 userId=0)
        if (request.method.equals(HttpMethod.GET.name(), ignoreCase = true)) {
            val isReviewsUri = request.requestURI.startsWith("/reviews")
            val isMyReviewUri = request.requestURI == "/reviews/me"
            val isPrivate = request.getParameter("is_private")?.toBoolean() ?: false
            if (isReviewsUri && !isMyReviewUri && !isPrivate) {
                setAnonymousPrincipal() // authenticated=false
                filterChain.doFilter(request, response)
                return
            }
        }

        // 4) 메뉴 API 특례
        if (request.method.equals(HttpMethod.GET.name(), ignoreCase = true) &&
            menuGetPattern.matches(request.requestURI)
        ) {
            if (token.isNullOrBlank()) {
                setAnonymousPrincipal()
                filterChain.doFilter(request, response)
                return
            } else {
                runCatching {
                    val userId = verifyAndExtractUserId(token)
                    setAuthenticatedPrincipal(userId)
                    filterChain.doFilter(request, response)
                }.onFailure {
                    SecurityContextHolder.clearContext()
                    // 여기선 401을 바로 내려도 됨. 필터에서 예외를 던지면 전역 핸들러만 탈 수 있음.
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
                return
            }
        }

        // 5) 그 외: 토큰 필수
        if (token.isNullOrBlank()) {
            SecurityContextHolder.clearContext()
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
            return
        } else {
            runCatching {
                val userId = verifyAndExtractUserId(token)
                setAuthenticatedPrincipal(userId)
            }.onFailure {
                SecurityContextHolder.clearContext()
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun isPermitAll(request: HttpServletRequest): Boolean = permitAllMatchers.any { it.matches(request) }

    private fun verifyAndExtractUserId(token: String): Int {
        val claims = jwtProvider.verifyJwtGetClaims(token)
        val value = claims[USER_ID_KEY_IN_JWT] ?: error("no userId claim")
        return when (value) {
            is Int -> value
            is Long -> value.toInt()
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: error("bad userId")
            else -> error("bad userId type")
        }
    }

    private fun setAuthenticatedPrincipal(userId: Int) {
        // authenticated=true
        val auth = UsernamePasswordAuthenticationToken(UserPrincipal(userId), null, emptyList())
        SecurityContextHolder.getContext().authentication = auth
    }

    private fun setAnonymousPrincipal() {
        // authenticated=false 로 유지 (중요)
        val auth = UsernamePasswordAuthenticationToken(UserPrincipal(ANONYMOUS_USER_ID), null)
        SecurityContextHolder.getContext().authentication = auth
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val raw =
            request.getHeader("Authorization")?.takeIf { it.isNotBlank() }
                ?: request.getHeader(AUTHORIZATION_HEADER_NAME)?.takeIf { it.isNotBlank() }
        return raw?.removePrefix("Bearer ")?.trim()
    }

    companion object {
        private const val USER_ID_KEY_IN_JWT = "userId" // 실제 클레임 키와 맞추세요 (예: "sub")
        private const val AUTHORIZATION_HEADER_NAME = "Authorization-Token"
        private const val ANONYMOUS_USER_ID = 0
    }
}

val HttpServletRequest.userId: Int
    get() {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return (principal as? UserPrincipal)?.userId ?: throw UnauthorizedUserException()
    }

data class UserPrincipal(val userId: Int)
