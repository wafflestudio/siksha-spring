package siksha.wafflestudio.core.domain.common.exception.user

import org.springframework.http.HttpStatus
import siksha.wafflestudio.core.domain.common.exception.SikshaException

sealed class UserException(httpStatus: HttpStatus, errorMessage: String) : SikshaException(httpStatus, errorMessage)

class UserNotFoundException : UserException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다.")

class DuplicatedNicknameException : UserException(HttpStatus.CONFLICT, "중복된 닉네임이 존재합니다.")

class BannedWordException : UserException(HttpStatus.BAD_REQUEST, "사용이 불가능한 단어가 포함되어 있습니다.")
