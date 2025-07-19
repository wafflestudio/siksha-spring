package siksha.wafflestudio.api.common

import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val authInterceptor: AuthInterceptor,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
            .excludePathPatterns(
                "/community/**/web",
                "/error",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/actuator/health",
                "/restaurants",
                "/auth/privacy-policy",
                "/auth/login/**",
                "/auth/nicknames/validate"
            )
    }
}

