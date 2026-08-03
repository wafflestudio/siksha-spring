package siksha.wafflestudio.api.common

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import siksha.wafflestudio.core.domain.auth.JwtProvider
import siksha.wafflestudio.core.domain.common.exception.auth.UnauthorizedUserException

private const val USER_ID = "userId"
private const val AUTHORIZATION = "Authorization"
private const val BEARER_PREFIX = "Bearer "

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    @Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver,
) : OncePerRequestFilter() {
    companion object {
        private val permitAllMatchers =
            listOf(
                AntPathRequestMatcher("/community/boards", HttpMethod.GET.name()),
                AntPathRequestMatcher("/community/boards/{board_id}", HttpMethod.GET.name()),
                AntPathRequestMatcher("/community/**/web"),
                AntPathRequestMatcher("/menus/**/web"),
                AntPathRequestMatcher("/reviews/**/web"),
                AntPathRequestMatcher("/v2/**/web"),
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
                AntPathRequestMatcher("/v2/reviews/dist"),
                AntPathRequestMatcher("/v2/reviews/keyword/dist"),
                AntPathRequestMatcher("/voc"),
                AntPathRequestMatcher("/ping"),
                AntPathRequestMatcher("/versions/**", HttpMethod.GET.name()),
                AntPathRequestMatcher("/menus/festival/**"),
            )
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.method.equals(HttpMethod.OPTIONS.name(), ignoreCase = true) ||
            permitAllMatchers.any { it.matches(request) } ||
            CrawlerApiKeyFilter.requiresApiKey(request)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val token = extractBearerToken(request)
        if (token == null) {
            unauthorized(request, response)
            return
        }

        val userId =
            runCatching { verifyAndExtractUserId(token) }
                .getOrElse {
                    unauthorized(request, response)
                    return
                }

        request.setAttribute(USER_ID, userId)
        chain.doFilter(request, response)
    }

    private fun extractBearerToken(request: HttpServletRequest): String? =
        request
            .getHeader(AUTHORIZATION)
            ?.takeIf { it.startsWith(BEARER_PREFIX, ignoreCase = true) }
            ?.substring(BEARER_PREFIX.length)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    private fun verifyAndExtractUserId(token: String): Int {
        val claims = jwtProvider.verifyJwtGetClaims(token)
        val value = claims[USER_ID] ?: throw UnauthorizedUserException()
        return when (value) {
            is Int -> value
            is Long -> value.toInt()
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: throw UnauthorizedUserException()
            else -> throw UnauthorizedUserException()
        }
    }

    private fun unauthorized(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        resolver.resolveException(request, response, null, UnauthorizedUserException())
    }
}

/** 컨트롤러/서비스 단에서 userId 꺼낼 때 사용하는 확장 프로퍼티 */
val HttpServletRequest.userId: Int
    get() = (getAttribute(USER_ID) as? Int) ?: throw UnauthorizedUserException()
