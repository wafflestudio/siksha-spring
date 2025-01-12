package siksha.wafflestudio.core.domain.common.exception

import org.springframework.http.HttpStatus

sealed class CommunityException(httpStatus: HttpStatus, errorMessage: String) : SikshaException(httpStatus, errorMessage)

class BoardNotFoundException : CommunityException(HttpStatus.NOT_FOUND, "Board not found")

class BoardNameAlreadyExistException : CommunityException(HttpStatus.BAD_REQUEST, "Board name already exists")

class InvalidBoardFormException(message: String) : CommunityException(HttpStatus.BAD_REQUEST, message)

class InvalidPageNumberException(): CommunityException(HttpStatus.NOT_FOUND, "잘못된 페이지 번호입니다.")

class InvalidPostFormException(message: String) : CommunityException(HttpStatus.BAD_REQUEST, message)

class S3ImageUploadException() : CommunityException(HttpStatus.SERVICE_UNAVAILABLE, "AWS S3 오류")
