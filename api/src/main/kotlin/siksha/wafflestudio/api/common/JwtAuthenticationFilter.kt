package siksha.wafflestudio.api.common

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import siksha.wafflestudio.core.domain.auth.JwtProvider
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    @Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver,
) : OncePerRequestFilter() {
    // GET /menus 또는 /menus/{menu_id}
    private val menuGetPattern = Regex("^/menus(?:/\\d+)?$")

    // SecurityConfig의 permitAll과 "항상" 동기화해 주세요.
    private val permitAllMatchers =
        listOf(
            AntPathRequestMatcher("/community/boards", HttpMethod.GET.name()),
            AntPathRequestMatcher("/community/**/web"),
            AntPathRequestMatcher("/error"),
            AntPathRequestMatcher("/swagger-ui/**"),
            AntPathRequestMatcher("/v3/api-docs/**"),
            AntPathRequestMatcher("/actuator/health"),
            AntPathRequestMatcher("/restaurants"),
            AntPathRequestMatcher("/auth/privacy-policy"),
            AntPathRequestMatcher("/auth/login/**"),
            AntPathRequestMatcher("/auth/nicknames/validate"),
            AntPathRequestMatcher("/docs"),
        )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        // 0) CORS preflight는 무조건 통과
        if (request.method.equals(HttpMethod.OPTIONS.name(), ignoreCase = true)) {
            chain.doFilter(request, response)
            return
        }

        // 1) SecurityConfig에서 permitAll 처리된 경로는 즉시 통과
        if (permitAllMatchers.any { it.matches(request) }) {
            chain.doFilter(request, response)
            return
        }

        val token = extractBearerToken(request)

        // 2) [특례] GET /reviews: is_private=false인 공개 리뷰는 익명으로 허용
        if (request.method.equals(HttpMethod.GET.name(), ignoreCase = true) &&
            request.requestURI.startsWith("/reviews") && request.requestURI != "/reviews/me"
        ) {
            val isPrivate = request.getParameter("is_private")?.toBoolean() ?: false
            if (!isPrivate) {
                // [변경] 익명 사용자 인증 정보 설정
                setAnonymousAuthentication(request)
                chain.doFilter(request, response)
                return
            }
        }

        // 3) [특례] GET /menus(/{id}): 토큰 없으면 익명, 있으면 검증
        if (request.method.equals(HttpMethod.GET.name(), ignoreCase = true) &&
            menuGetPattern.matches(request.requestURI)
        ) {
            if (token.isNullOrBlank()) {
                // [변경] 익명 사용자 인증 정보 설정
                setAnonymousAuthentication(request)
                chain.doFilter(request, response)
                return
            }
        }

        // 4) 위 특례에 해당하지 않는 모든 보호된 경로: 토큰 필수 검증
        if (token.isNullOrBlank()) {
            unauthorized(request, response)
            return
        }

        // 5) 토큰 검증 및 userId 설정
        runCatching {
            val userId = verifyAndExtractUserId(token)
            // [변경] 인증된 사용자 정보 설정
            setAuthenticatedUser(userId, request)
        }.getOrElse {
            unauthorized(request, response)
            return
        }

        // 6) 다음 필터로 전달
        chain.doFilter(request, response)
    }

    // ... 기존 extractBearerToken, verifyAndExtractUserId, unauthorized 함수 ...

    private fun extractBearerToken(request: HttpServletRequest): String? {
        val h = request.getHeader(HDR_AUTHORIZATION) ?: return null
        if (!h.startsWith(BEARER_PREFIX, ignoreCase = true)) return null
        return h.substring(BEARER_PREFIX.length).trim().takeIf { it.isNotEmpty() }
    }

    private fun verifyAndExtractUserId(token: String): Int {
        val claims = jwtProvider.verifyJwtGetClaims(token)
        val v = claims[USER_ID_CLAIM] ?: error("no userId claim")
        return when (v) {
            is Int -> v
            is Long -> v.toInt()
            is Number -> v.toInt()
            is String -> v.toIntOrNull() ?: error("bad userId")
            else -> error("bad userId type")
        }
    }

    private fun unauthorized(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        resolver.resolveException(request, response, null, UnauthorizedUserException())
    }

    private fun setAnonymousAuthentication(request: HttpServletRequest) {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        val authentication = UsernamePasswordAuthenticationToken(ANONYMOUS_USER_ID, null, authorities)
        SecurityContextHolder.getContext().authentication = authentication
        request.setAttribute(REQ_ATTR_USER_ID, ANONYMOUS_USER_ID)
    }

    private fun setAuthenticatedUser(
        userId: Int,
        request: HttpServletRequest,
    ) {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authentication = UsernamePasswordAuthenticationToken(userId, null, authorities)
        SecurityContextHolder.getContext().authentication = authentication
        request.setAttribute(REQ_ATTR_USER_ID, userId)
    }

    companion object {
        private const val USER_ID_CLAIM = "userId"
        private const val REQ_ATTR_USER_ID = "userId"
        private const val HDR_AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val ANONYMOUS_USER_ID = 0
    }
}

/** 컨트롤러/서비스 단에서 userId 꺼낼 때 사용 */
val HttpServletRequest.userId: Int
    get() = (this.getAttribute("userId") as? Int) ?: throw UnauthorizedUserException()
