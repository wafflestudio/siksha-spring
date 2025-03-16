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
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException
import javax.crypto.SecretKey

@Component
class AuthInterceptor(
    @Value("\${jwt.secret-key}") private val jwtSecretKey: String,
) : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var encodedJwtSecretKey: SecretKey

    init {
        // 기존 api 서버에서 기준보다 짧은 secret을 사용중이어서, 호환성을 위해 padding
        val extendedKey = jwtSecretKey.toByteArray().copyOf(32)
        encodedJwtSecretKey = Keys.hmacShaKeyFor(extendedKey)
    }


    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (request.method == HttpMethod.GET.name() && request.requestURI == "/community/boards") return true
        if (request.method == HttpMethod.OPTIONS.name()) return true

        return runCatching {
            val authHeader = request.getHeader(AUTHORIZATION_HEADER_NAME)
            val userId = verifyJwtGetClaims(authHeader)[USER_ID_KEY_IN_JWT]!! as Int
            request.setAttribute("userId", userId)
            true
        }.getOrElse {
            logger.error("auth failed")
            throw UnauthorizedUserException()
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
        private const val USER_ID_KEY_IN_JWT = "userId"
        private const val AUTHORIZATION_HEADER_NAME = "Authorization-Token"
    }
}

val HttpServletRequest.userId: Int
    get() = this.getAttribute("userId") as Int
