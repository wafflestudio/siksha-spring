package siksha.wafflestudio.core.domain.common.exception.community

import org.springframework.http.HttpStatus

class CustomNotFoundException(vararg items: NotFoundItem) : CommunityException(
    HttpStatus.NOT_FOUND,
    "다음을 찾을 수 없습니다: " + items.joinToString(),
)

class InvalidPageNumberException() : CommunityException(HttpStatus.NOT_FOUND, "잘못된 페이지 번호입니다.")

enum class NotFoundItem(val value: String) {
    USER("유저"),
    POST("게시물"),
    BOARD("게시판"),
}
