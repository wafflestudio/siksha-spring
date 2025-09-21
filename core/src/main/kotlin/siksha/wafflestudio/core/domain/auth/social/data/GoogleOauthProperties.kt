package siksha.wafflestudio.core.domain.auth.social.data

import org.springframework.boot.context.properties.ConfigurationProperties

// Bean 주입용
@ConfigurationProperties(prefix = "siksha.oauth.google")
data class GoogleOauthProperties(
    val clientId: Map<String, String> = emptyMap(),
)
