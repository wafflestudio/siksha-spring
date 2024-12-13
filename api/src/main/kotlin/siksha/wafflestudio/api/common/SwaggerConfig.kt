package siksha.wafflestudio.api.common

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openApi(): OpenAPI {
        return OpenAPI()
            .info(configurationInfo())
    }

    private fun configurationInfo(): Info {
        return Info()
            .title("Siksha Spring")
            .version("v1.0.0")
            .description("식샤 스프링 API Docs")
    }
}
