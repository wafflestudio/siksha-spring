package siksha.wafflestudio.api.common

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import siksha.wafflestudio.core.domain.common.exception.UnauthorizedUserException

@Component
class CrawlerApiKeyFilter(
    @Value("\${siksha.crawler.api-key:}") private val expectedApiKey: String,
    @Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver,
) : OncePerRequestFilter() {
    companion object {
        private const val HDR_API_KEY = "X-API-Key"
        private const val CRAWLER_PATH_PREFIX = "/crawler/"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (!request.requestURI.startsWith(CRAWLER_PATH_PREFIX)) {
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
}
