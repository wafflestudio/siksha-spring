package siksha.wafflestudio.core.domain.common.exception.auth

import org.springframework.http.HttpStatus
import siksha.wafflestudio.core.domain.common.exception.SikshaException

sealed class AuthException(httpStatus: HttpStatus, errorMessage: String) : SikshaException(httpStatus, errorMessage)

class UnauthorizedUserException : AuthException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다.")

class TokenParseException : AuthException(HttpStatus.UNAUTHORIZED, "인증 토큰 형식이 잘못되었습니다.")

class InvalidSSOTokenException : AuthException(HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패했습니다.")

class SSOProviderException : AuthException(HttpStatus.SERVICE_UNAVAILABLE, "소셜 로그인에 실패했습니다.")
