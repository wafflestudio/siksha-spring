package siksha.wafflestudio.api.common

import io.jsonwebtoken.Claims
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.servlet.HandlerExceptionResolver
import siksha.wafflestudio.core.domain.auth.JwtProvider
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JwtAuthenticationFilterTest {
    private val jwtProvider = mockk<JwtProvider>()
    private val resolver = mockk<HandlerExceptionResolver>(relaxed = true)
    private val filter = JwtAuthenticationFilter(jwtProvider, resolver)

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @ParameterizedTest
    @MethodSource("publicRequests")
    fun `allow current public request without jwt`(
        method: String,
        path: String,
    ) {
        val request = request(method, path)
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        verify(exactly = 0) { resolver.resolveException(any(), any(), any(), any()) }
        verify(exactly = 1) { chain.doFilter(request, response) }
    }

    @ParameterizedTest
    @MethodSource("crawlerAuthenticatedRequests")
    fun `allow crawler key owned request to bypass jwt`(
        method: String,
        path: String,
    ) {
        val request = request(method, path)
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        verify(exactly = 0) { resolver.resolveException(any(), any(), any(), any()) }
        verify(exactly = 1) { chain.doFilter(request, response) }
    }

    @ParameterizedTest
    @MethodSource("protectedRequests")
    fun `reject protected request without jwt`(
        method: String,
        path: String,
    ) {
        val request = request(method, path)
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        verify(exactly = 1) { resolver.resolveException(request, response, null, any()) }
        verify(exactly = 0) { chain.doFilter(any(), any()) }
    }

    @Test
    fun `expose authenticated user id without populating spring security context`() {
        val claims = mockk<Claims>()
        every { claims["userId"] } returns 42
        every { jwtProvider.verifyJwtGetClaims("valid-token") } returns claims
        val request =
            request("GET", "/auth/me").apply {
                addHeader("Authorization", "Bearer valid-token")
            }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        assertEquals(42, request.getAttribute("userId"))
        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 1) { chain.doFilter(request, response) }
    }

    @Test
    fun `reject invalid jwt`() {
        every { jwtProvider.verifyJwtGetClaims("invalid-token") } throws IllegalArgumentException("invalid jwt")
        val request =
            request("GET", "/auth/me").apply {
                addHeader("Authorization", "Bearer invalid-token")
            }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        verify(exactly = 1) { resolver.resolveException(request, response, null, any()) }
        verify(exactly = 0) { chain.doFilter(any(), any()) }
    }

    private fun request(
        method: String,
        path: String,
    ): MockHttpServletRequest =
        MockHttpServletRequest(method, path).apply {
            servletPath = path
        }

    companion object {
        @JvmStatic
        fun publicRequests(): List<Arguments> =
            listOf(
                Arguments.of("OPTIONS", "/auth/me"),
                Arguments.of("GET", "/community/boards"),
                Arguments.of("GET", "/community/boards/1"),
                Arguments.of("GET", "/community/posts/web"),
                Arguments.of("GET", "/menus/1/web"),
                Arguments.of("GET", "/reviews/1/web"),
                Arguments.of("GET", "/v2/menus/web"),
                Arguments.of("GET", "/error"),
                Arguments.of("GET", "/swagger-ui/index.html"),
                Arguments.of("GET", "/v3/api-docs/siksha"),
                Arguments.of("GET", "/docs"),
                Arguments.of("GET", "/actuator/health"),
                Arguments.of("GET", "/restaurants"),
                Arguments.of("GET", "/auth/privacy-policy"),
                Arguments.of("POST", "/auth/login/apple"),
                Arguments.of("GET", "/auth/nicknames/validate"),
                Arguments.of("GET", "/reviews/comments/recommendation"),
                Arguments.of("GET", "/reviews/dist"),
                Arguments.of("GET", "/reviews/keyword/dist"),
                Arguments.of("GET", "/v2/reviews/dist"),
                Arguments.of("GET", "/v2/reviews/keyword/dist"),
                Arguments.of("POST", "/voc"),
                Arguments.of("GET", "/ping"),
                Arguments.of("GET", "/versions/IOS"),
                Arguments.of("GET", "/menus/festival/dates"),
            )

        @JvmStatic
        fun crawlerAuthenticatedRequests(): List<Arguments> =
            listOf(
                Arguments.of("POST", "/v2/crawler/meals"),
                Arguments.of("PATCH", "/versions/IOS"),
            )

        @JvmStatic
        fun protectedRequests(): List<Arguments> =
            listOf(
                Arguments.of("GET", "/auth/me"),
                Arguments.of("POST", "/community/boards"),
                Arguments.of("GET", "/menus"),
                Arguments.of("GET", "/v2/restaurants/personal"),
                Arguments.of("DELETE", "/versions/IOS"),
            )
    }
}
