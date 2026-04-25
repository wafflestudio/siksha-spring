package siksha.wafflestudio.core.domain.common.exception.community

import org.springframework.http.HttpStatus

class InvalidBoardFormException(message: String) : CommunityException(HttpStatus.BAD_REQUEST, message)

class BoardNotFoundException : CommunityException(HttpStatus.NOT_FOUND, "해당 게시판을 찾을 수 없습니다.")

class BoardSaveFailedException(message: String?) : CommunityException(HttpStatus.INTERNAL_SERVER_ERROR, "게시판 저장에 실패하였습니다 - $message")

class BoardNameAlreadyExistException : CommunityException(HttpStatus.CONFLICT, "중복된 게시판 이름이 존재합니다.")
