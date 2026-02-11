package siksha.wafflestudio.core.domain.common.exception.community

import org.springframework.http.HttpStatus

class InvalidPostFormException(message: String) : CommunityException(HttpStatus.BAD_REQUEST, message)

class NotPostOwnerException : CommunityException(HttpStatus.FORBIDDEN, "해당 글의 작성자가 아닙니다.")

class PostNotFoundException : CommunityException(HttpStatus.NOT_FOUND, "해당 글을 찾을 수 없습니다.")

class InvalidPostReportFormException() : CommunityException(HttpStatus.BAD_REQUEST, "이유는 1자에서 200자 사이여야 합니다.")

class PostAlreadyReportedException() : CommunityException(HttpStatus.CONFLICT, "이미 신고된 글입니다.")

class PostReportSaveFailedException() : CommunityException(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 신고에 실패하였습니다.")
