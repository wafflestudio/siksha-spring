package siksha.wafflestudio.core.domain.auth.social

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import siksha.wafflestudio.core.domain.common.exception.SikshaException
import java.time.Duration

@Component("kakao")
class KakaoTokenVerifier(
    @Value("\${spring.security.oauth2.client.registration.kakao.client-id}") private val kakaoAppId: String,
    private val restTemplate: RestTemplate
) : SocialTokenVerifier {

    private data class KakaoTokenInfo(
        @JsonProperty("id") val id: Long,
        @JsonProperty("app_id") val appId: Long
    )

    override fun verify(token: String): String {
        val headers = HttpHeaders().apply {
            set(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
        val entity = HttpEntity<String>(headers)

        val responseEntity = try {
            restTemplate.exchange(
                KAKAO_TOKEN_INFO_URL,
                HttpMethod.GET,
                entity,
                KakaoTokenInfo::class.java
            )
        } catch (e: Exception) {
            throw SikshaException(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 서버 통신에 실패했습니다.") // 예외 처리 수정
        }

        val response = responseEntity.body ?: throw SikshaException(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 서버로부터 응답을 받지 못했습니다.") // 예외 처리 수정

        if (response.appId.toString() != kakaoAppId) {
            throw SikshaException(HttpStatus.UNAUTHORIZED, "유효하지 않은 카카오 토큰입니다. (앱 ID 불일치)")
        }

        return response.id.toString()
    }

    companion object {
        private const val KAKAO_TOKEN_INFO_URL = "https://kapi.kakao.com/v1/user/access_token_info"
        // RestTemplate은 timeout을 직접 설정하지 않으므로 TIMEOUT_SECONDS는 사용하지 않습니다.
    }
}
