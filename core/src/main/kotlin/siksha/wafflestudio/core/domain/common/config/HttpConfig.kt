package siksha.wafflestudio.core.domain.common.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class HttpConfig {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate =
        builder
            .setConnectTimeout(java.time.Duration.ofSeconds(3))
            .setReadTimeout(java.time.Duration.ofSeconds(3))
            .build()
}
