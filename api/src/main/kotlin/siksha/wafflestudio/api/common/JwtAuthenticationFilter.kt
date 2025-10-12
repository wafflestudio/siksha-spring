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
    companion object {
        private const val USER_ID_CLAIM = "userId"
        private const val REQ_ATTR_USER_ID = "userId"
        private const val HDR_AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    private val permitAllMatchers =
        listOf(
            AntPathRequestMatcher("/community/boards", HttpMethod.GET.name()),
            AntPathRequestMatcher("/community/boards/{board_id}", HttpMethod.GET.name()),
            AntPathRequestMatcher("/community/**/web"),
            AntPathRequestMatcher("/menus/**/web"),
            AntPathRequestMatcher("/reviews/**/web"),
            AntPathRequestMatcher("/error"),
            AntPathRequestMatcher("/swagger-ui/**"),
            AntPathRequestMatcher("/v3/api-docs/**"),
            AntPathRequestMatcher("/docs"),
            AntPathRequestMatcher("/actuator/health"),
            AntPathRequestMatcher("/restaurants"),
            AntPathRequestMatcher("/auth/privacy-policy"),
            AntPathRequestMatcher("/auth/login/**"),
            AntPathRequestMatcher("/auth/nicknames/validate"),
            AntPathRequestMatcher("/reviews/comments/recommendation"),
            AntPathRequestMatcher("/reviews/dist"),
            AntPathRequestMatcher("/reviews/keyword/dist"),
        )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        // CORS preflight 요청은 무조건 통과
        if (request.method.equals(HttpMethod.OPTIONS.name(), ignoreCase = true)) {
            chain.doFilter(request, response)
            return
        }

        // permitAll 경로는 인증 없이 통과
        if (permitAllMatchers.any { it.matches(request) }) {
            chain.doFilter(request, response)
            return
        }

        // Bearer 토큰 추출
        val token = extractBearerToken(request)
        if (token == null) {
            unauthorized(request, response)
            return
        }

        // 토큰 검증 및 사용자 인증 설정
        runCatching {
            val userId = verifyAndExtractUserId(token)
            setAuthenticatedUser(userId, request)
        }.onFailure {
            unauthorized(request, response)
            return
        }

        chain.doFilter(request, response)
    }

    private fun extractBearerToken(request: HttpServletRequest): String? =
        request.getHeader(HDR_AUTHORIZATION)
            ?.takeIf { it.startsWith(BEARER_PREFIX, ignoreCase = true) }
            ?.substring(BEARER_PREFIX.length)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    private fun verifyAndExtractUserId(token: String): Int {
        val claims = jwtProvider.verifyJwtGetClaims(token)
        val value = claims[USER_ID_CLAIM] ?: error("no userId claim")
        return when (value) {
            is Int -> value
            is Long -> value.toInt()
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: error("bad userId")
            else -> error("bad userId type")
        }
    }

    private fun unauthorized(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        resolver.resolveException(request, response, null, UnauthorizedUserException())
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
}

/** 컨트롤러/서비스 단에서 userId 꺼낼 때 사용하는 확장 프로퍼티 */
val HttpServletRequest.userId: Int
    get() = (getAttribute("userId") as? Int) ?: throw UnauthorizedUserException()
