package siksha.wafflestudio.core.domain.common.exception

import org.springframework.http.HttpStatus

sealed class CommunityException(httpStatus: HttpStatus, errorMessage: String) : SikshaException(httpStatus, errorMessage)

class InvalidBoardFormException(message: String) : CommunityException(HttpStatus.BAD_REQUEST, message)

class InvalidPostFormException(message: String) : CommunityException(HttpStatus.BAD_REQUEST, message)

class UnauthorizedUserException : CommunityException(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다.")

class NotCommentOwnerException: CommunityException(HttpStatus.FORBIDDEN, "해당 댓글의 작성자가 아닙니다.")

class NotPostOwnerException: CommunityException(HttpStatus.FORBIDDEN, "해당 글의 작성자가 아닙니다.")

class CustomNotFoundException(vararg items: NotFoundItem): CommunityException(HttpStatus.NOT_FOUND, "다음을 찾을 수 없습니다: " + items.joinToString())

class InvalidPageNumberException(): CommunityException(HttpStatus.NOT_FOUND, "잘못된 페이지 번호입니다.")

class BoardNotFoundException : CommunityException(HttpStatus.NOT_FOUND, "해당 게시판을 찾을 수 없습니다.")

class UserNotFoundException: CommunityException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다.")

class PostNotFoundException: CommunityException(HttpStatus.NOT_FOUND, "해당 글을 찾을 수 없습니다.")

class CommentNotFoundException: CommunityException(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다.")

class RestaurantNotFound : SikshaException(HttpStatus.NOT_FOUND, "Restaurant not found")

class BoardNameAlreadyExistException : CommunityException(HttpStatus.CONFLICT, "중복된 게시판 이름이 존재합니다.")

class ImageUploadFailedException : CommunityException(HttpStatus.SERVICE_UNAVAILABLE, "이미지 업로드 실패")

class CommentAlreadyReportedException() : CommunityException(HttpStatus.CONFLICT, "이미 신고된 댓글입니다.")

class InvalidCommentReportFormException() : CommunityException(HttpStatus.BAD_REQUEST, "이유는 1자에서 200자 사이여야 합니다.")

class PostAlreadyReportedException() : CommunityException(HttpStatus.CONFLICT, "이미 신고된 글입니다.")

class InvalidPostReportFormException() : CommunityException(HttpStatus.BAD_REQUEST, "이유는 1자에서 200자 사이여야 합니다.")

class CommentReportSaveFailedException(): CommunityException(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 신고에 실패하였습니다.")

class PostReportSaveFailedException(): CommunityException(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 신고에 실패하였습니다.")

enum class NotFoundItem(val value: String) {
    USER("유저"),
    POST("게시물"),
    BOARD("게시판")
}
