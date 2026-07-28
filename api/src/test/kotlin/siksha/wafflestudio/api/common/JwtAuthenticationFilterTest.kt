package siksha.wafflestudio.api.common

import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.servlet.HandlerExceptionResolver
import siksha.wafflestudio.core.domain.auth.JwtProvider

class JwtAuthenticationFilterTest {
    private val jwtProvider = mockk<JwtProvider>(relaxed = true)
    private val resolver = mockk<HandlerExceptionResolver>(relaxed = true)
    private val filter = JwtAuthenticationFilter(jwtProvider, resolver)

    @Test
    fun `allow version endpoints without jwt because api key filter owns update authentication`() {
        val request =
            MockHttpServletRequest("PATCH", "/versions/IOS").apply {
                servletPath = "/versions/IOS"
            }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        verify(exactly = 0) { resolver.resolveException(any(), any(), any(), any()) }
        verify(exactly = 1) { chain.doFilter(request, response) }
    }
}
