package siksha.wafflestudio.core.domain.common.exception.community

import org.springframework.http.HttpStatus

class NotCommentOwnerException : CommunityException(HttpStatus.FORBIDDEN, "해당 댓글의 작성자가 아닙니다.")

class CommentNotFoundException : CommunityException(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다.")

class CommentAlreadyReportedException() : CommunityException(HttpStatus.CONFLICT, "이미 신고된 댓글입니다.")

class InvalidCommentReportFormException() : CommunityException(HttpStatus.BAD_REQUEST, "이유는 1자에서 200자 사이여야 합니다.")

class CommentReportSaveFailedException() : CommunityException(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 신고에 실패하였습니다.")
