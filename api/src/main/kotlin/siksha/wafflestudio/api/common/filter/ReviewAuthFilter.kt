package siksha.wafflestudio.api.common.filter

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import siksha.wafflestudio.api.common.util.AuthResult
import siksha.wafflestudio.api.common.util.AuthUtils
import siksha.wafflestudio.api.common.util.JwtTokenHandler

@Component
class ReviewAuthFilter(
    private val jwtTokenHandler: JwtTokenHandler,
) {
    fun filterReviewApi(
        request: HttpServletRequest,
        token: String?,
    ): AuthResult {
        // Use servletPath to get path without query parameters
        val path = request.servletPath ?: request.requestURI.split('?')[0]
        val endpoint = "${request.method} $path"

        when {
            // 1) APIs that ALWAYS require authentication
            endpoint in ALWAYS_AUTHENTICATED_ENDPOINTS ||
                isAlwaysAuthenticatedPattern(request.method, path) -> {
                val userId =
                    jwtTokenHandler.validateTokenAndGetUserId(token)
                        ?: return AuthResult.UNAUTHORIZED

                AuthUtils.setLoginUser(userId, request)
                return AuthResult.SUCCESS
            }

            // 2) APIs that NEVER require authentication
            endpoint in NEVER_AUTHENTICATED_ENDPOINTS -> {
                AuthUtils.setGuestUser(request)
                return AuthResult.SUCCESS
            }

            // 3) APIs with conditional authentication (is_login parameter)
            endpoint in CONDITIONAL_AUTH_ENDPOINTS ||
                isConditionalAuthPattern(request.method, path) -> {
                val isLogin = request.getParameter("is_login")?.toBoolean() ?: false
                if (!isLogin) {
                    AuthUtils.setGuestUser(request)
                } else {
                    val userId =
                        jwtTokenHandler.validateTokenAndGetUserId(token)
                            ?: return AuthResult.UNAUTHORIZED
                    AuthUtils.setLoginUser(userId, request)
                }
                return AuthResult.SUCCESS
            }

            // 4) Fallback: require authentication for unknown review APIs
            else -> {
                val userId =
                    jwtTokenHandler.validateTokenAndGetUserId(token)
                        ?: return AuthResult.UNAUTHORIZED

                AuthUtils.setLoginUser(userId, request)
                return AuthResult.SUCCESS
            }
        }
    }

    private fun isAlwaysAuthenticatedPattern(
        method: String,
        path: String,
    ): Boolean {
        return when {
            // All POST/PATCH/DELETE operations require auth
            method in listOf("POST", "PATCH", "DELETE") -> true
            else -> false
        }
    }

    private fun isConditionalAuthPattern(
        method: String,
        path: String,
    ): Boolean {
        return method == "GET" && path.matches(Regex("^/reviews/\\d+$"))
    }

    companion object {
        private val ALWAYS_AUTHENTICATED_ENDPOINTS =
            setOf(
                "GET /reviews/me",
            )

        private val NEVER_AUTHENTICATED_ENDPOINTS =
            setOf(
                "GET /reviews/comments/recommendation",
                "GET /reviews/dist",
                "GET /reviews/keyword/dist",
            )

        private val CONDITIONAL_AUTH_ENDPOINTS =
            setOf(
                "GET /reviews",
                "GET /reviews/filter",
            )
    }
}
