package siksha.wafflestudio.core.domain.common.exception

import org.springframework.http.HttpStatus

sealed class RestaurantException(
    httpStatus: HttpStatus,
    errorMessage: String,
) : SikshaException(httpStatus, errorMessage)

class RestaurantNotFoundException : RestaurantException(HttpStatus.NOT_FOUND, "해당 식당을 찾을 수 없습니다.")

class BuildingNotFoundException : RestaurantException(HttpStatus.NOT_FOUND, "Building not found")

class InvalidRestaurantOrderException : RestaurantException(HttpStatus.BAD_REQUEST, "Invalid restaurant order")

class InvalidCustomException : RestaurantException(HttpStatus.BAD_REQUEST, "Invalid custom")
