package siksha.wafflestudio.core.domain.auth.social

import com.nimbusds.jwt.JWTClaimsSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.URI

@Component
class SocialTokenVerifierImpl(
    @Value("\${siksha.oauth.kakao.app-id}")
    private val kakaoAppId: Long,
    private val rest: RestTemplate
) : SocialTokenVerifier {

    override fun verifyGoogleIdToken(idToken: String, clientId: String): SocialProfile {
        val c = verifyOidc(idToken,
            issuer = "https://accounts.google.com",
            jwksUri = "https://www.googleapis.com/oauth2/v3/certs",
            requiredAud = clientId
        )
        return SocialProfile("google", c.subject)
    }

    override fun verifyAppleIdToken(idToken: String, clientId: String): SocialProfile {
        val c = verifyOidc(idToken,
            issuer = "https://appleid.apple.com",
            jwksUri = "https://appleid.apple.com/auth/keys",
            requiredAud = clientId
        )
        return SocialProfile("apple", c.subject)
    }

    override fun verifyKakaoIdToken(idToken: String, clientId: String): SocialProfile {
        val jwksUri = discoverJwks("https://kauth.kakao.com/.well-known/openid-configuration")
        val c = verifyOidc(idToken,
            issuer = "https://kauth.kakao.com",
            jwksUri = jwksUri,
            requiredAud = clientId
        )
        return SocialProfile("kakao", c.subject)
    }

    override fun verifyKakaoAccessToken(accessToken: String): SocialProfile {
        val headers = HttpHeaders().apply { setBearerAuth(accessToken) }
        val entity = HttpEntity<Void>(headers)

        val info = rest.exchange(
            URI("https://kapi.kakao.com/v1/user/access_token_info"),
            HttpMethod.GET, entity, KakaoTokenInfo::class.java
        ).body ?: error("kakao token info null")

        require(info.app_id == kakaoAppId) { "app_id mismatch" } // 우리 앱 토큰인지 확인
        return SocialProfile("kakao", info.id.toString())
    }

    // ---- OIDC 공통 ----
    private fun verifyOidc(idToken: String, issuer: String, jwksUri: String, requiredAud: String): JWTClaimsSet {
        val jwt = com.nimbusds.jwt.SignedJWT.parse(idToken)
        val jwkSource = com.nimbusds.jose.jwk.source.RemoteJWKSet<com.nimbusds.jose.proc.SecurityContext>(java.net.URL(jwksUri))
        val selector = com.nimbusds.jose.proc.JWSVerificationKeySelector<com.nimbusds.jose.proc.SecurityContext>(
            jwt.header.algorithm as com.nimbusds.jose.JWSAlgorithm, jwkSource
        )
        val processor = com.nimbusds.jwt.proc.DefaultJWTProcessor<com.nimbusds.jose.proc.SecurityContext>().apply {
            jwsKeySelector = selector
            jwtClaimsSetVerifier = com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier(
                JWTClaimsSet.Builder().issuer(issuer).build(),
                setOf("sub", "aud", "exp")
            )
        }
        val claims = processor.process(jwt, null)
        require(claims.audience.contains(requiredAud)) { "aud mismatch" }
        return claims
    }

    private fun discoverJwks(oidcConfigUrl: String): String {
        val doc = rest.getForObject(oidcConfigUrl, Map::class.java) ?: error("OIDC discovery failed")
        return doc["jwks_uri"] as String
    }
}
