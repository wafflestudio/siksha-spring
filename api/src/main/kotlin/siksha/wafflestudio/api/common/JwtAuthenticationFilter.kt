package siksha.wafflestudio.api.common

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import siksha.wafflestudio.core.domain.auth.JwtProvider

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(request)

        if (token != null) {
            try {
                val claims = jwtProvider.verifyJwtGetClaims(token)
                val userId = claims[USER_ID_KEY_IN_JWT] as Int

                val authentication = UsernamePasswordAuthenticationToken(
                    UserPrincipal(userId),
                    null,
                    emptyList()
                )
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                SecurityContextHolder.clearContext()
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val token = request.getHeader("Authorization")
            ?.takeIf { it.isNotBlank() }
            ?: request.getHeader(AUTHORIZATION_HEADER_NAME)?.takeIf { it.isNotBlank() }
        return token?.removePrefix("Bearer ")
    }

    companion object {
        private const val USER_ID_KEY_IN_JWT = "userId"
        private const val AUTHORIZATION_HEADER_NAME = "Authorization-Token"
    }
}

val HttpServletRequest.userId: Int
    get() = (SecurityContextHolder.getContext().authentication?.principal as? UserPrincipal)?.userId as Int

data class UserPrincipal(
    val userId: Int,
)
