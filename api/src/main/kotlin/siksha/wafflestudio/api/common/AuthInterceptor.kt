package siksha.wafflestudio.api.common

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import siksha.wafflestudio.core.domain.auth.JwtProvider
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException

@Component
class AuthInterceptor(
    @Value("\${jwt.secret-key}") private val jwtSecretKey: String,
    private val jwtProvider: JwtProvider,
) : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (request.method == HttpMethod.GET.name() && request.requestURI == "/community/boards") return true
        if (request.method == HttpMethod.OPTIONS.name()) return true

        return runCatching {
            val authHeader = request.getHeader(AUTHORIZATION_HEADER_NAME)
            val userId = jwtProvider.verifyJwtGetClaims(authHeader)[USER_ID_KEY_IN_JWT]!! as Int
            request.setAttribute("userId", userId)
            true
        }.getOrElse {
            logger.error("auth failed")
            throw UnauthorizedUserException()
        }
    }

    companion object {
        private const val USER_ID_KEY_IN_JWT = "userId"
        private const val AUTHORIZATION_HEADER_NAME = "Authorization-Token"
    }
}

val HttpServletRequest.userId: Int
    get() = this.getAttribute("userId") as Int
