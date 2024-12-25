package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.comment.dto.GetCommentsResponseDto
import siksha.wafflestudio.core.domain.comment.service.CommentService

@RestController
class CommentController(
    private val commentService: CommentService,
) {
    @GetMapping("/community/comments/web")
    fun getCommentsWithoutAuth(
        request: HttpServletRequest,
        @RequestParam(name = "post_id") postId: Long,
        @RequestParam(name = "page") page: Int,
        @RequestParam(name = "per_page") perPage: Int,
    ): GetCommentsResponseDto? {
        return commentService.getCommentsWithoutAuth(page, perPage)
    }
}
