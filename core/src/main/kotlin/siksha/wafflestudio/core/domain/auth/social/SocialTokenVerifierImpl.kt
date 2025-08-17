package siksha.wafflestudio.core.domain.auth.social

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.RemoteJWKSet
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import siksha.wafflestudio.core.domain.auth.social.data.GoogleOauthProperties
import siksha.wafflestudio.core.domain.auth.social.data.KakaoTokenInfo
import siksha.wafflestudio.core.domain.auth.social.data.SocialProfile
import siksha.wafflestudio.core.domain.auth.social.data.SocialProvider
import siksha.wafflestudio.core.domain.common.exception.SSOProviderException
import java.net.URI
import java.net.URL

@Component
class SocialTokenVerifierImpl(
    private val googleProps: GoogleOauthProperties,
    @Value("\${siksha.oauth.apple.approved-audience}")  private val appleClientIds: List<String>,
    @Value("\${siksha.oauth.kakao.app-id}")
    private val kakaoAppId: Long,
    private val rest: RestTemplate
) : SocialTokenVerifier {
    private lateinit var googleClientIds: List<String>
    @PostConstruct
    fun init() {
        googleClientIds = googleProps.clientId.values.filter { it.isNotBlank() }
    }

    override fun verifyGoogleIdToken(idToken: String): SocialProfile {
        val c = verifyOidc(
            idToken,
            issuer = "https://accounts.google.com",
            jwksUri = "https://www.googleapis.com/oauth2/v3/certs",
            requiredAudiences = googleClientIds
        )
        return SocialProfile(SocialProvider.GOOGLE, c.subject)
    }

    override fun verifyAppleIdToken(idToken: String): SocialProfile {
        val c = verifyOidc(
            idToken,
            issuer = "https://appleid.apple.com",
            jwksUri = "https://appleid.apple.com/auth/keys",
            requiredAudiences = appleClientIds
        )
        return SocialProfile(SocialProvider.APPLE, c.subject)
    }

    override fun verifyKakaoAccessToken(accessToken: String): SocialProfile {
        val headers = HttpHeaders().apply { setBearerAuth(accessToken) }
        val entity = HttpEntity<Void>(headers)

        val info = rest.exchange(
            URI("https://kapi.kakao.com/v1/user/access_token_info"),
            HttpMethod.GET, entity, KakaoTokenInfo::class.java
        ).body ?: throw SSOProviderException()

        require(info.app_id == kakaoAppId) { "app_id mismatch" } // 우리 앱 토큰인지 확인
        return SocialProfile(SocialProvider.KAKAO, info.id.toString())
    }

    private fun verifyOidc(idToken: String, issuer: String, jwksUri: String, requiredAudiences: List<String>): JWTClaimsSet {
        val jwt = SignedJWT.parse(idToken)
        val jwkSource = RemoteJWKSet<SecurityContext>(URL(jwksUri))
        val selector = JWSVerificationKeySelector(
            jwt.header.algorithm as JWSAlgorithm, jwkSource
        )
        val processor = DefaultJWTProcessor<SecurityContext>().apply {
            jwsKeySelector = selector
            jwtClaimsSetVerifier = com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier(
                JWTClaimsSet.Builder().issuer(issuer).build(),
                setOf("sub", "aud", "exp")
            )
        }
        val claims = processor.process(jwt, null)
        val audList = claims.audience.orEmpty()
        val azp = claims.getStringClaim("azp")
        val allowed = requiredAudiences.filter { !it.isNullOrBlank() }.toSet()

        val match = audList.any { it in allowed } || (azp != null && azp in allowed)
        require(match) {
            "aud mismatch: aud=$audList, azp=$azp, allowed=$allowed"
        }
        return claims
    }
}
