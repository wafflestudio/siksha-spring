package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.comment.dto.*
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
        return commentService.getCommentsWithoutAuth(postId, page, perPage)
    }

    @GetMapping("/community/comments")
    fun getComments(
        request: HttpServletRequest,
        @RequestParam(name = "post_id") postId: Long,
        @RequestParam(name = "page") page: Int,
        @RequestParam(name = "per_page") perPage: Int,
    ): GetCommentsResponseDto? {
        return commentService.getComments(request.userId,postId, page, perPage)
    }

    @PostMapping("/community/comments")
    @ResponseStatus(HttpStatus.CREATED)
    fun createComment(
        request: HttpServletRequest,
        @RequestBody createDto: CreateCommentRequestDto,
    ): CommentResponseDto? {
        return commentService.createComment(request.userId, createDto)
    }

    @PatchMapping("/community/comments/{commentId}")
    fun patchComment(
        request: HttpServletRequest,
        @PathVariable commentId: Long,
        @RequestBody patchDto: PatchCommentRequestDto,
    ): CommentResponseDto {
        return commentService.patchComment(request.userId, commentId, patchDto)
    }

    @DeleteMapping("/community/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteComment(
        request: HttpServletRequest,
        @PathVariable commentId: Long,
    ) {
        commentService.deleteComment(request.userId, commentId)
    }

    @PostMapping("/community/comments/{commentId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    fun createCommentLike(
        request: HttpServletRequest,
        @PathVariable commentId: Long,
    ): CommentResponseDto {
        return commentService.createOrUpdateCommentLike(request.userId, commentId, true)
    }

    //TODO: delete 방식으로 수정
    @PostMapping("/community/comments/{commentId}/unlike")
    @ResponseStatus(HttpStatus.CREATED)
    fun createCommentUnlike(
        request: HttpServletRequest,
        @PathVariable commentId: Long,
    ): CommentResponseDto {
        return commentService.createOrUpdateCommentLike(request.userId, commentId, false)
    }

    @PostMapping("/community/comments/{commentId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    fun createCommentReport(
        request: HttpServletRequest,
        @PathVariable commentId: Long,
        @RequestBody createDto: CreateCommentReportRequestDto,
    ): CommentsReportResponseDto {
        return commentService.createCommentReport(request.userId, commentId, createDto.reason)
    }
}
