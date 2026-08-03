package siksha.wafflestudio.core.domain.common.exception.main

import org.springframework.http.HttpStatus
import siksha.wafflestudio.core.domain.common.exception.SikshaException

sealed class MainException(
    httpStatus: HttpStatus,
    errorMessage: String,
) : SikshaException(httpStatus, errorMessage)

class RestaurantNotFound : MainException(HttpStatus.NOT_FOUND, "Restaurant not found")
