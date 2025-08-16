package siksha.wafflestudio.core.domain.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret-key}") private val jwtSecretKey: String,
) {
    private lateinit var encodedJwtSecretKey: SecretKey

    init {
        // 기존 api 서버에서 기준보다 짧은 secret을 사용중이어서, 호환성을 위해 padding
        val extendedKey = jwtSecretKey.toByteArray().copyOf(32)
        encodedJwtSecretKey = Keys.hmacShaKeyFor(extendedKey)
    }

    fun generateAccessToken(
        userId: Int,
        lifetimeInDays: Long,
    ): String {
        val exp = Date.from(Instant.now().plus(lifetimeInDays, ChronoUnit.DAYS))

        return Jwts.builder()
            .expiration(exp)
            .claim("userId", userId)
            .signWith(encodedJwtSecretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun verifyJwtGetClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(encodedJwtSecretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
