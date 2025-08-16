package siksha.wafflestudio.core.domain.auth.social

import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.common.exception.SikshaException
import java.net.URL

@Component("google")
class GoogleTokenVerifier(
    @Value("\${spring.security.oauth2.client.registration.google.client-id}") private val googleAud: String,
    @Value("\${spring.security.oauth2.client.provider.google.issuer-uri}") private val googleIss: String
) : SocialTokenVerifier {

    override fun verify(token: String): String {
        try {
            val jwkSet = JWKSourceBuilder.create<com.nimbusds.jose.proc.SecurityContext>(URL(GOOGLE_JWKS_URL)).build()
            val processor = DefaultJWTProcessor<com.nimbusds.jose.proc.SecurityContext>()
            processor.jwsKeySelector = com.nimbusds.jose.proc.JWSVerificationKeySelector(
                com.nimbusds.jose.JWSAlgorithm.RS256,
                jwkSet
            )

            val claims = processor.process(token, null)

            if (claims.issuer != googleIss) {
                throw SikshaException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Google 토큰입니다. (iss 불일치)")
            }
            if (!claims.audience.contains(googleAud)) {
                // 참고: Google의 aud는 여러 값일 수 있으나, 우리 앱의 client-id는 반드시 포함해야 함
                throw SikshaException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Google 토큰입니다. (aud 불일치)")
            }

            return claims.subject
        } catch (e: Exception) {
            throw SikshaException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Google 토큰입니다.")
        }
    }

    companion object {
        private const val GOOGLE_JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs"
    }
}
