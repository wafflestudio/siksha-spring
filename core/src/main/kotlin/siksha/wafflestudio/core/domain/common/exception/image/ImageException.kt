package siksha.wafflestudio.core.domain.common.exception.image

import org.springframework.http.HttpStatus
import siksha.wafflestudio.core.domain.common.exception.SikshaException

sealed class ImageException(httpStatus: HttpStatus, errorMessage: String) : SikshaException(httpStatus, errorMessage)

class ImageUploadFailedException : ImageException(HttpStatus.SERVICE_UNAVAILABLE, "이미지 업로드 실패")
