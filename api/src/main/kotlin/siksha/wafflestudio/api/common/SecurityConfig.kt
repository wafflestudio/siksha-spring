package siksha.wafflestudio.api.common

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                // set whitelist here
                it.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                  .requestMatchers(HttpMethod.GET, "/community/boards").permitAll()
                  .requestMatchers(AntPathRequestMatcher.antMatcher("/community/**/web")).permitAll()
                  .requestMatchers(
                      "/error",
                      "/swagger-ui/**",
                      "/v3/api-docs/**",
                      "/actuator/health",
                      "/restaurants",
                      "/auth/privacy-policy",
                      "/auth/login/**",
                      "/auth/nicknames/validate",
                  ).permitAll()
                  .anyRequest().authenticated() // 나머지 모든 요청은 인증 필요
            }
            // 우리가 만든 JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 이전에 실행
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { handler ->
                handler.authenticationEntryPoint { _, response, _ -> // 인증 실패 시
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증되지 않은 사용자입니다.")
                }
                handler.accessDeniedHandler { _, response, _ -> // 인가 실패 시
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근이 거부되었습니다.")
                }
            }
            .build()
    }
}
