package siksha.wafflestudio.core.domain.common.exception.version

import org.springframework.http.HttpStatus
import siksha.wafflestudio.core.domain.common.exception.SikshaException

sealed class VersionException(
    httpStatus: HttpStatus,
    errorMessage: String,
) : SikshaException(httpStatus, errorMessage)

class InvalidClientTypeException(
    clientType: String,
) : VersionException(HttpStatus.BAD_REQUEST, "지원하지 않는 client_type 입니다: $clientType")

class VersionNotFoundException(
    clientType: String,
) : VersionException(HttpStatus.NOT_FOUND, "해당 client_type 의 버전 정보를 찾을 수 없습니다: $clientType")
