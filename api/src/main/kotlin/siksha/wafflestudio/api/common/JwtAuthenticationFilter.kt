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
            AntPathRequestMatcher("/actuator/health"),
            AntPathRequestMatcher("/restaurants"),
            AntPathRequestMatcher("/auth/privacy-policy"),
            AntPathRequestMatcher("/auth/login/**"),
            AntPathRequestMatcher("/auth/nicknames/validate"),
            AntPathRequestMatcher("/docs"),
            AntPathRequestMatcher("/reviews/comments/recommendation"),
            AntPathRequestMatcher("/reviews/dist"),
            AntPathRequestMatcher("/reviews/keyword/dist"),
        )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (request.method.equals(HttpMethod.OPTIONS.name(), ignoreCase = true)) {
            chain.doFilter(request, response)
            return
        }

        if (permitAllMatchers.any { it.matches(request) }) {
            chain.doFilter(request, response)
            return
        }

        val token = extractBearerToken(request)
        if (token.isNullOrBlank()) {
            unauthorized(request, response)
            return
        }
        runCatching {
            val userId = verifyAndExtractUserId(token)
            setAuthenticatedUser(userId, request)
        }.getOrElse {
            unauthorized(request, response)
            return
        }

        chain.doFilter(request, response)
    }

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
