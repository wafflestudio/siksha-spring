package siksha.wafflestudio.core.domain.common.exception

import org.springframework.http.HttpStatus

sealed class MainException(httpStatus: HttpStatus, errorMessage: String) : SikshaException(httpStatus, errorMessage)
class UserNotFoundException: MainException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다.")
