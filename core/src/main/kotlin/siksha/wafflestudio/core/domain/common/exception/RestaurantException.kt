package siksha.wafflestudio.core.domain.common.exception

import org.springframework.http.HttpStatus

sealed class RestaurantException(
    httpStatus: HttpStatus,
    errorMessage: String,
) : SikshaException(httpStatus, errorMessage)

class RestaurantNotFoundException : RestaurantException(HttpStatus.NOT_FOUND, "해당 식당을 찾을 수 없습니다.")

class BuildingNotFoundException : RestaurantException(HttpStatus.NOT_FOUND, "해당 건물을 찾을 수 없습니다.")

class InvalidRestaurantOrderException : RestaurantException(HttpStatus.BAD_REQUEST, "식당 또는 건물 순서 요청이 잘못되었습니다.")
