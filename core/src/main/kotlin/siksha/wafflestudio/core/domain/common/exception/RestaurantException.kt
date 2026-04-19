package siksha.wafflestudio.core.domain.common.exception

import org.springframework.http.HttpStatus

sealed class RestaurantException(
    httpStatus: HttpStatus,
    errorMessage: String,
) : SikshaException(httpStatus, errorMessage)

class RestaurantNotFoundException : RestaurantException(HttpStatus.NOT_FOUND, "해당 식당을 찾을 수 없습니다.")
