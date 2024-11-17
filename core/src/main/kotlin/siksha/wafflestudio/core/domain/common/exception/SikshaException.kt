package siksha.wafflestudio.core.domain.common.exception

import org.springframework.http.HttpStatus

open class SikshaException(
    val httpStatus: HttpStatus,
    val errorMessage: String,
) : RuntimeException(errorMessage)

data class ErrorBody(
    // 추후 code 추가 가능
    val message: String,
)

class RestaurantNotFound : SikshaException(HttpStatus.NOT_FOUND, "Restaurant not found")
