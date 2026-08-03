package siksha.wafflestudio.api.common

import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.servlet.HandlerExceptionResolver

class CrawlerApiKeyFilterTest {
    private val resolver = mockk<HandlerExceptionResolver>(relaxed = true)
    private val filter = CrawlerApiKeyFilter("crawler-key", resolver)

    @ParameterizedTest
    @MethodSource("crawlerAuthenticatedRequests")
    fun `reject crawler authenticated request without api key`(
        method: String,
        path: String,
    ) {
        val request = MockHttpServletRequest(method, path)
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        verify(exactly = 1) { resolver.resolveException(request, response, null, any()) }
        verify(exactly = 0) { chain.doFilter(any(), any()) }
    }

    @ParameterizedTest
    @MethodSource("crawlerAuthenticatedRequests")
    fun `allow crawler authenticated request with api key`(
        method: String,
        path: String,
    ) {
        val request =
            MockHttpServletRequest(method, path).apply {
                addHeader("X-API-Key", "crawler-key")
            }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        verify(exactly = 0) { resolver.resolveException(any(), any(), any(), any()) }
        verify(exactly = 1) { chain.doFilter(request, response) }
    }

    @Test
    fun `allow public version lookup without crawler api key`() {
        val request = MockHttpServletRequest("GET", "/versions/IOS")
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        verify(exactly = 0) { resolver.resolveException(any(), any(), any(), any()) }
        verify(exactly = 1) { chain.doFilter(request, response) }
    }

    @Test
    fun `reject crawler authenticated request with invalid api key`() {
        val request =
            MockHttpServletRequest("POST", "/v2/crawler/meals").apply {
                addHeader("X-API-Key", "invalid-key")
            }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        verify(exactly = 1) { resolver.resolveException(request, response, null, any()) }
        verify(exactly = 0) { chain.doFilter(any(), any()) }
    }

    companion object {
        @JvmStatic
        fun crawlerAuthenticatedRequests(): List<Arguments> =
            listOf(
                Arguments.of("POST", "/v2/crawler/meals"),
                Arguments.of("PATCH", "/versions/IOS"),
            )
    }
}
