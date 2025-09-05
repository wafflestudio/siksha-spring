package siksha.wafflestudio.core.domain.common.exception

import org.springframework.http.HttpStatus

sealed class MainException(httpStatus: HttpStatus, errorMessage: String) : SikshaException(httpStatus, errorMessage)

class RestaurantNotFound : MainException(HttpStatus.NOT_FOUND, "Restaurant not found")

class UserNotFoundException : MainException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다.")

class InvalidScoreException : MainException(HttpStatus.BAD_REQUEST, "평점은 1에서 5 사이여야 합니다.")

/**
 * Auth 시 토큰이 무효한 경우 사용
 * 토큰은 유효하지만 DB에 userId에 해당하는 User가 없는 경우에도 사용
 */
class UnauthorizedUserException : MainException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다.")

class MenuNotFoundException : MainException(HttpStatus.NOT_FOUND, "해당 메뉴를 찾을 수 없습니다.")

class MenuLikeException : MainException(HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 좋아요 처리 중에 오류가 발생했습니다.")

class DuplicatedNicknameException : MainException(HttpStatus.CONFLICT, "중복된 닉네임이 존재합니다.")

class BannedWordException : MainException(HttpStatus.BAD_REQUEST, "사용이 불가능한 단어가 포함되어 있습니다.")

class KeywordMissingException : MainException(HttpStatus.BAD_REQUEST, "작성하지 않은 키워드 리뷰가 존재합니다.")

class ReviewAlreadyExistsException : MainException(HttpStatus.CONFLICT, "이 메뉴에 대한 리뷰가 이미 존재합니다")

class ReviewSaveFailedException : MainException(HttpStatus.INTERNAL_SERVER_ERROR, "리뷰 저장 중에 오류가 발생했습니다.")

class SelfReviewLikeNotAllowedException : MainException(HttpStatus.BAD_REQUEST, "본인의 리뷰에는 좋아요를 누를 수 없습니다.")
