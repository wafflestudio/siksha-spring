package siksha.wafflestudio.api.common.filter

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import siksha.wafflestudio.api.common.util.AuthResult
import siksha.wafflestudio.api.common.util.AuthUtils
import siksha.wafflestudio.api.common.util.JwtTokenHandler

@Component
class MenuAuthFilter(
    private val jwtTokenHandler: JwtTokenHandler,
) {
    fun filterMenuApi(
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

            // 2) APIs with conditional authentication (is_login parameter)
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

            // 3) Fallback: require authentication for unknown menu APIs
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
        return method == "POST"
    }

    private fun isConditionalAuthPattern(
        method: String,
        path: String,
    ): Boolean {
        return method == "GET" && path.matches(Regex("^/menus/\\d+$"))
    }

    companion object {
        private val ALWAYS_AUTHENTICATED_ENDPOINTS =
            setOf(
                "GET /menus/me",
            )

        private val CONDITIONAL_AUTH_ENDPOINTS =
            setOf(
                "GET /menus",
            )
    }
}
