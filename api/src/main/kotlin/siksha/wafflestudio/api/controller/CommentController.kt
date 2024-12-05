package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.comment.repository.CommentRepository
import siksha.wafflestudio.core.domain.comment.service.CommentService

@RestController
class CommentController(
    private val commentService: CommentService,
) {
    @GetMapping("/comments")
    fun getComments(
        request: HttpServletRequest
    ) {
        // FIXME: JWT secret 찾아서 parameter store에 넣은 뒤 request.userId 추가
        val tempUserId = 181L
        commentService.getComments(tempUserId)
    }
}
