package siksha.wafflestudio.api.common.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import siksha.wafflestudio.api.common.util.AuthResult
import siksha.wafflestudio.api.common.util.AuthUtils
import siksha.wafflestudio.api.common.util.JwtTokenHandler
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException

@Component
class JwtAuthenticationFilter(
    private val jwtTokenHandler: JwtTokenHandler,
    private val reviewAuthFilter: ReviewAuthFilter,
    private val menuAuthFilter: MenuAuthFilter,
    @Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver,
) : OncePerRequestFilter() {
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

        val token = jwtTokenHandler.extractBearerToken(request)

        when {
            // 2) /reviews: 인증 필터링 로직
            request.requestURI.startsWith("/reviews") -> {
                val result = reviewAuthFilter.filterReviewApi(request, token)
                when (result) {
                    AuthResult.UNAUTHORIZED -> {
                        unauthorized(request, response)
                        return
                    }
                    AuthResult.SUCCESS -> chain.doFilter(request, response)
                }
            }

            // 3) /menus: 인증 필터링 로직
            request.requestURI.startsWith("/menus") -> {
                val result = menuAuthFilter.filterMenuApi(request, token)
                when (result) {
                    AuthResult.UNAUTHORIZED -> {
                        unauthorized(request, response)
                        return
                    }
                    AuthResult.SUCCESS -> chain.doFilter(request, response)
                }
            }

            // 4) 기타 보호된 경로: 토큰 필수 검증
            else -> {
                val userId =
                    jwtTokenHandler.validateTokenAndGetUserId(token)
                        ?: run {
                            unauthorized(request, response)
                            return
                        }

                AuthUtils.setLoginUser(userId, request)
                chain.doFilter(request, response)
            }
        }
    }

    private fun unauthorized(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        resolver.resolveException(request, response, null, UnauthorizedUserException())
    }
}

val HttpServletRequest.userId: Int
    get() = (this.getAttribute(AuthUtils.REQ_ATTR_USER_ID) as? Int) ?: throw UnauthorizedUserException()
