package siksha.wafflestudio.api.common

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.nio.charset.StandardCharsets

@Component
class AuthInterceptor(
    @Value("\${jwt.secret-key}") private val jwtSecretKey: String,
) : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val encodedJwtSecretKey = Keys.hmacShaKeyFor(jwtSecretKey.toByteArray(StandardCharsets.UTF_8))

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (request.method == HttpMethod.OPTIONS.name()) return true

        return runCatching {
            val authHeader = request.getHeader(AUTHORIZATION_HEADER_NAME)
            val userId = verifyJwtGetClaims(authHeader)[USER_ID_KEY_IN_JWT]
            request.setAttribute("userId", userId)
            true
        }.getOrElse {
            logger.error("auth failed")
            throw it
        }
    }

    fun verifyJwtGetClaims(token: String): Claims {
        val trimmed = token.removePrefix("Bearer ")
        return Jwts.parser()
            .verifyWith(encodedJwtSecretKey)
            .build()
            .parseSignedClaims(trimmed)
            .payload
    }

    companion object {
        private const val USER_ID_KEY_IN_JWT = "user_id"
        private const val AUTHORIZATION_HEADER_NAME = "Authorization-Token"
    }
}

val HttpServletRequest.userId: Long
    get() = this.getAttribute("userId") as Long
