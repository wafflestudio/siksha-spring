package siksha.wafflestudio.api.common

import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.servlet.HandlerExceptionResolver

class CrawlerApiKeyFilterTest {
    private val resolver = mockk<HandlerExceptionResolver>(relaxed = true)
    private val filter = CrawlerApiKeyFilter("crawler-key", resolver)

    @Test
    fun `reject version update without crawler api key`() {
        val request = MockHttpServletRequest("PATCH", "/versions/IOS")
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        verify(exactly = 1) { resolver.resolveException(request, response, null, any()) }
        verify(exactly = 0) { chain.doFilter(any(), any()) }
    }

    @Test
    fun `allow version update with crawler api key`() {
        val request =
            MockHttpServletRequest("PATCH", "/versions/IOS").apply {
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
}
