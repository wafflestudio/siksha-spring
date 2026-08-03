package siksha.wafflestudio.api.common

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import siksha.wafflestudio.core.domain.common.exception.auth.UnauthorizedUserException

@Component
class CrawlerApiKeyFilter(
    @Value("\${siksha.crawler.api-key:}") private val expectedApiKey: String,
    @Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver,
) : OncePerRequestFilter() {
    companion object {
        private const val HDR_API_KEY = "X-API-Key"
        private const val CRAWLER_PATH_PREFIX = "/v2/crawler/"
        private const val VERSION_PATH_PREFIX = "/versions/"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (!requiresCrawlerApiKey(request)) {
            chain.doFilter(request, response)
            return
        }

        val apiKey = request.getHeader(HDR_API_KEY)
        if (apiKey.isNullOrBlank() || apiKey != expectedApiKey) {
            resolver.resolveException(request, response, null, UnauthorizedUserException())
            return
        }

        chain.doFilter(request, response)
    }

    private fun requiresCrawlerApiKey(request: HttpServletRequest): Boolean =
        request.requestURI.startsWith(CRAWLER_PATH_PREFIX) ||
            (
                request.method.equals(HttpMethod.PATCH.name(), ignoreCase = true) &&
                    request.requestURI.startsWith(VERSION_PATH_PREFIX)
            )
}
