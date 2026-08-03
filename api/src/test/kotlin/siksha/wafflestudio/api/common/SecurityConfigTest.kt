package siksha.wafflestudio.api.common

import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.web.FilterChainProxy
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.web.filter.CorsFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import siksha.wafflestudio.core.domain.auth.JwtProvider
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringJUnitConfig(classes = [SecurityConfig::class, SecurityConfigTest.TestConfig::class])
@WebAppConfiguration
class SecurityConfigTest(
    @param:Autowired private val filterChainProxy: FilterChainProxy,
    @param:Autowired @param:Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver,
) {
    @Test
    fun `cors filter runs before custom authentication filters`() {
        val filters = filterChainProxy.filterChains.single().filters
        val corsFilterIndex = filters.indexOfFirst { it is CorsFilter }
        val crawlerApiKeyFilterIndex = filters.indexOfFirst { it is CrawlerApiKeyFilter }
        val jwtAuthenticationFilterIndex = filters.indexOfFirst { it is JwtAuthenticationFilter }

        assertTrue(corsFilterIndex >= 0)
        assertTrue(crawlerApiKeyFilterIndex >= 0)
        assertTrue(jwtAuthenticationFilterIndex >= 0)
        assertTrue(corsFilterIndex < crawlerApiKeyFilterIndex)
        assertTrue(corsFilterIndex < jwtAuthenticationFilterIndex)
    }

    @ParameterizedTest
    @ValueSource(strings = ["/auth/me", "/v2/crawler/meals"])
    fun `allow cors preflight before authentication filters`(path: String) {
        val origin = "https://siksha.example.com"
        val request =
            MockHttpServletRequest("OPTIONS", path).apply {
                servletPath = path
                addHeader(HttpHeaders.ORIGIN, origin)
                addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
            }
        val response = MockHttpServletResponse()
        val downstreamChain = mockk<FilterChain>(relaxed = true)

        filterChainProxy.doFilter(request, response, downstreamChain)

        assertEquals(200, response.status)
        assertEquals(origin, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
        verify(exactly = 0) { resolver.resolveException(any(), any(), any(), any()) }
        verify(exactly = 0) { downstreamChain.doFilter(any(), any()) }
    }

    @Configuration
    class TestConfig {
        @Bean
        fun jwtAuthenticationFilter(
            jwtProvider: JwtProvider,
            @Qualifier("handlerExceptionResolver") resolver: HandlerExceptionResolver,
        ) = JwtAuthenticationFilter(jwtProvider, resolver)

        @Bean
        fun crawlerApiKeyFilter(
            @Qualifier("handlerExceptionResolver") resolver: HandlerExceptionResolver,
        ) = CrawlerApiKeyFilter("crawler-key", resolver)

        @Bean
        fun jwtProvider(): JwtProvider = mockk()

        @Bean("handlerExceptionResolver")
        fun handlerExceptionResolver(): HandlerExceptionResolver = mockk(relaxed = true)
    }
}
