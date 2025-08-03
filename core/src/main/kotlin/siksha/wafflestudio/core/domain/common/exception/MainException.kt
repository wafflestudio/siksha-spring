package siksha.wafflestudio.core.domain.common.exception

import org.springframework.http.HttpStatus

sealed class MainException(httpStatus: HttpStatus, errorMessage: String) : SikshaException(httpStatus, errorMessage)

class RestaurantNotFound : MainException(HttpStatus.NOT_FOUND, "Restaurant not found")

class InvalidScoreException : MainException(HttpStatus.BAD_REQUEST, "score는 1에서 5 사이여야 합니다.")

class MenuNotFoundException : MainException(HttpStatus.NOT_FOUND, "해당 메뉴를 찾을 수 없습니다.")
