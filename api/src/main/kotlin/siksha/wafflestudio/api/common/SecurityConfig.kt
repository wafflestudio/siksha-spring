package siksha.wafflestudio.api.common

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.HandlerExceptionResolver
import siksha.wafflestudio.core.domain.common.exception.auth.UnauthorizedUserException

private typealias AuthorizationRegistry = AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    @Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .cors { }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { configureAuthorization(it) }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { configureExceptionHandling(it) }
            .build()

    private fun configureAuthorization(auth: AuthorizationRegistry) {
        auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/community/boards", "/community/boards/{board_id}").permitAll()
            .requestMatchers(
                AntPathRequestMatcher.antMatcher("/community/**/web"),
                AntPathRequestMatcher.antMatcher("/menus/**/web"),
                AntPathRequestMatcher.antMatcher("/reviews/**/web"),
            ).permitAll()
            .requestMatchers(
                "/error",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/actuator/health",
                "/restaurants",
                "/auth/privacy-policy",
                "/auth/login/**",
                "/auth/nicknames/validate",
                "/docs",
                "/reviews/comments/recommendation",
                "/reviews/dist",
                "/reviews/keyword/dist",
                "/voc",
                "/ping",
            ).permitAll()
            .anyRequest().authenticated()
    }

    private fun configureExceptionHandling(handler: ExceptionHandlingConfigurer<HttpSecurity>) {
        handler
            .authenticationEntryPoint { request, response, _ ->
                resolver.resolveException(request, response, null, UnauthorizedUserException())
            }
            .accessDeniedHandler { request, response, _ ->
                resolver.resolveException(request, response, null, UnauthorizedUserException())
            }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource =
        UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration(
                "/**",
                CorsConfiguration().apply {
                    allowedOriginPatterns = listOf("*")
                    allowedMethods = listOf("*")
                    allowedHeaders = listOf("*")
                },
            )
        }
}
