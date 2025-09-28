package siksha.wafflestudio.api.common.util

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.auth.JwtProvider

@Component
class JwtTokenHandler(
    private val jwtProvider: JwtProvider,
) {
    fun extractBearerToken(request: HttpServletRequest): String? {
        val h = request.getHeader(HDR_AUTHORIZATION) ?: return null
        if (!h.startsWith(BEARER_PREFIX, ignoreCase = true)) return null
        return h.substring(BEARER_PREFIX.length).trim().takeIf { it.isNotEmpty() }
    }

    fun validateTokenAndGetUserId(token: String?): Int? {
        if (token.isNullOrBlank()) return null

        return runCatching {
            verifyAndExtractUserId(token)
        }.getOrNull()
    }

    fun verifyAndExtractUserId(token: String): Int {
        val claims = jwtProvider.verifyJwtGetClaims(token)
        val v = claims[USER_ID_CLAIM] ?: error("no userId claim")
        return when (v) {
            is Int -> v
            is Long -> v.toInt()
            is Number -> v.toInt()
            is String -> v.toIntOrNull() ?: error("bad userId")
            else -> error("bad userId type")
        }
    }

    companion object {
        private const val USER_ID_CLAIM = "userId"
        private const val HDR_AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
}
