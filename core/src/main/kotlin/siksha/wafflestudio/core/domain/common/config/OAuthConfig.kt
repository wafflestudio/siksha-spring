package siksha.wafflestudio.core.domain.common.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import siksha.wafflestudio.core.domain.auth.social.data.GoogleOauthProperties

@EnableConfigurationProperties(GoogleOauthProperties::class)
@Configuration
class OAuthConfig
