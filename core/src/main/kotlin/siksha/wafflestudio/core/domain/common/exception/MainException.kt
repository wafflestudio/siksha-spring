package siksha.wafflestudio.core.domain.common.exception

import org.springframework.http.HttpStatus

sealed class MainException(httpStatus: HttpStatus, errorMessage: String) : SikshaException(httpStatus, errorMessage)

class RestaurantNotFound : MainException(HttpStatus.NOT_FOUND, "Restaurant not found")

class UserNotFoundException : MainException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다.")

class InvalidScoreException : MainException(HttpStatus.BAD_REQUEST, "평점은 1에서 5 사이여야 합니다.")

/**
 * Auth 시 토큰이 무효한 경우 사용
 * 토큰은 유효하지만 DB에 userId에 해당하는 User가 없는 경우에도 사용
 * 단, Header가 Bearer로 시작하지 않는 경우에는 InvalidTokenHeaderException 사용
 */
class UnauthorizedUserException : MainException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다.")

class MenuNotFoundException : MainException(HttpStatus.NOT_FOUND, "해당 메뉴를 찾을 수 없습니다.")

class MenuLikeException : MainException(HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 좋아요 처리 중에 오류가 발생했습니다.")

class DuplicatedNicknameException : MainException(HttpStatus.CONFLICT, "중복된 닉네임이 존재합니다.")

class BannedWordException : MainException(HttpStatus.BAD_REQUEST, "사용이 불가능한 단어가 포함되어 있습니다.")

class InvalidTokenHeaderException: MainException(HttpStatus.UNAUTHORIZED, "올바른 인증 토큰 형식이 아닙니다. (Bearer …)")

class InvalidSSOTokenException: MainException(HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패했습니다.")

class SSOProviderException: MainException(HttpStatus.SERVICE_UNAVAILABLE, "소셜 로그인에 실패했습니다.")
