package siksha.wafflestudio.api.common.util

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

enum class AuthResult {
    SUCCESS,
    UNAUTHORIZED,
}

object AuthUtils {
    const val REQ_ATTR_USER_ID = "userId"
    const val GUEST_USER_ID = 0

    fun setLoginUser(
        userId: Int,
        request: HttpServletRequest,
    ) {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authentication = UsernamePasswordAuthenticationToken(userId, null, authorities)
        SecurityContextHolder.getContext().authentication = authentication
        request.setAttribute(REQ_ATTR_USER_ID, userId)
    }

    fun setGuestUser(request: HttpServletRequest) {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_GUEST"))
        val authentication = UsernamePasswordAuthenticationToken(GUEST_USER_ID, null, authorities)
        SecurityContextHolder.getContext().authentication = authentication
        request.setAttribute(REQ_ATTR_USER_ID, GUEST_USER_ID)
    }
}
