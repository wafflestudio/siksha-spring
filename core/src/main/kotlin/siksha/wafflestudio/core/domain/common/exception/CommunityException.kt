package siksha.wafflestudio.core.domain.common.exception

import org.springframework.http.HttpStatus

sealed class CommunityException(httpStatus: HttpStatus, errorMessage: String) : SikshaException(httpStatus, errorMessage)

class InvalidBoardFormException(message: String) : CommunityException(HttpStatus.BAD_REQUEST, message)

<<<<<<< HEAD
class InvalidPageNumberException(): CommunityException(HttpStatus.NOT_FOUND, "잘못된 페이지 번호입니다.")

class InvalidPostFormException(message: String) : CommunityException(HttpStatus.BAD_REQUEST, message)

class S3ImageUploadException() : CommunityException(HttpStatus.SERVICE_UNAVAILABLE, "AWS S3 오류")
=======
class UnauthorizedUserException : CommunityException(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다.")

class NotCommentOwnerException: CommunityException(HttpStatus.FORBIDDEN, "해당 댓글의 작성자가 아닙니다.")

class CustomNotFoundException(vararg items: NotFoundItem): CommunityException(HttpStatus.NOT_FOUND, "다음을 찾을 수 없습니다: " + items.joinToString())

class BoardNotFoundException : CommunityException(HttpStatus.NOT_FOUND, "해당 게시판을 찾을 수 없습니다.")

class UserNotFoundException: CommunityException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다.")

class PostNotFoundException: CommunityException(HttpStatus.NOT_FOUND, "해당 글을 찾을 수 없습니다.")

class CommentNotFoundException: CommunityException(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다.")

class RestaurantNotFound : SikshaException(HttpStatus.NOT_FOUND, "Restaurant not found")

class BoardNameAlreadyExistException : CommunityException(HttpStatus.CONFLICT, "중복된 게시판 이름이 존재합니다.")

enum class NotFoundItem(val value: String) {
    USER("유저"),
    POST("게시물")
}
>>>>>>> f6f24d3 (댓글 api들 추가)
