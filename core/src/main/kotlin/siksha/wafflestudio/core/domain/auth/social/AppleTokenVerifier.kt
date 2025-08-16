package siksha.wafflestudio.core.domain.auth.social

import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.common.exception.SikshaException
import java.net.URL

@Component("apple")
class AppleTokenVerifier(
    @Value("\${spring.security.oauth2.client.registration.apple.client-id}") private val appleAud: String,
    @Value("\${spring.security.oauth2.client.provider.apple.issuer-uri}") private val appleIss: String
) : SocialTokenVerifier {

    override fun verify(token: String): String {
        try {
            val jwkSet = JWKSourceBuilder.create<com.nimbusds.jose.proc.SecurityContext>(URL(APPLE_JWKS_URL)).build()
            val processor = DefaultJWTProcessor<com.nimbusds.jose.proc.SecurityContext>()
            processor.jwsKeySelector = com.nimbusds.jose.proc.JWSVerificationKeySelector(
                com.nimbusds.jose.JWSAlgorithm.RS256,
                jwkSet
            )

            val claims = processor.process(token, null)
            
            if (claims.issuer != appleIss) {
                throw SikshaException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Apple 토큰입니다. (iss 불일치)")
            }
            if (!claims.audience.contains(appleAud)) {
                throw SikshaException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Apple 토큰입니다. (aud 불일치)")
            }
            
            return claims.subject
        } catch (e: Exception) {
            throw SikshaException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Apple 토큰입니다.")
        }
    }

    companion object {
        private const val APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys"
    }
}
