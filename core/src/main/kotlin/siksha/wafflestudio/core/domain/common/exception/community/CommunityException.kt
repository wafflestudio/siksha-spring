package siksha.wafflestudio.core.domain.common.exception.community

import org.springframework.http.HttpStatus
import siksha.wafflestudio.core.domain.common.exception.SikshaException

sealed class CommunityException(httpStatus: HttpStatus, errorMessage: String) : SikshaException(httpStatus, errorMessage)
