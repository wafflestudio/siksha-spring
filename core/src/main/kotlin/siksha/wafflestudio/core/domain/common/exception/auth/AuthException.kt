package siksha.wafflestudio.core.domain.common.exception.auth

import org.springframework.http.HttpStatus
import siksha.wafflestudio.core.domain.common.exception.SikshaException

sealed class AuthException(
    httpStatus: HttpStatus,
    errorMessage: String,
) : SikshaException(httpStatus, errorMessage)

/**
 * Auth 시 토큰이 무효한 경우 사용
 * 토큰은 유효하지만 DB에 userId에 해당하는 User가 없는 경우에도 사용
 * 단, Header가 Bearer로 시작하지 않는 경우에는 TokenParseException 사용
 */
class UnauthorizedUserException : AuthException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다.")

class TokenParseException : AuthException(HttpStatus.UNAUTHORIZED, "인증 토큰 형식이 잘못되었습니다.")

class InvalidSSOTokenException : AuthException(HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패했습니다.")

class SSOProviderException : AuthException(HttpStatus.SERVICE_UNAVAILABLE, "소셜 로그인에 실패했습니다.")
