package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
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
import siksha.wafflestudio.core.domain.community.comment.dto.CommentResponseDto
import siksha.wafflestudio.core.domain.community.comment.dto.CommentsReportResponseDto
import siksha.wafflestudio.core.domain.community.comment.dto.CreateCommentReportRequestDto
import siksha.wafflestudio.core.domain.community.comment.dto.CreateCommentRequestDto
import siksha.wafflestudio.core.domain.community.comment.dto.GetCommentsResponseDto
import siksha.wafflestudio.core.domain.community.comment.dto.PatchCommentRequestDto
import siksha.wafflestudio.core.domain.community.comment.service.CommentService

@RestController
@Validated
@Tag(name = "Comments", description = "커뮤니티 댓글 관리 엔드포인트")
class CommentController(
    private val commentService: CommentService,
) {
    @GetMapping("/community/comments/web")
    @Operation(summary = "댓글 목록 조회 (비로그인)", description = "특정 게시글의 댓글 목록을 조회합니다 (비로그인)")
    fun getCommentsWithoutAuth(
        request: HttpServletRequest,
        @RequestParam(name = "post_id") postId: Int,
        @RequestParam(name = "page", defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = "per_page", defaultValue = "10") @Min(1) perPage: Int,
    ): GetCommentsResponseDto? {
        return commentService.getCommentsWithoutAuth(postId, page, perPage)
    }

    @GetMapping("/community/comments")
    @Operation(summary = "댓글 목록 조회 (로그인)", description = "특정 게시글의 댓글 목록을 조회합니다 (로그인)")
    @SecurityRequirement(name = "bearerAuth")
    fun getComments(
        request: HttpServletRequest,
        @RequestParam(name = "post_id") postId: Int,
        @RequestParam(name = "page", defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = "per_page", defaultValue = "10") @Min(1) perPage: Int,
    ): GetCommentsResponseDto? {
        return commentService.getComments(request.userId, postId, page, perPage)
    }

    @PostMapping("/community/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "댓글 작성", description = "새로운 댓글을 작성합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun createComment(
        request: HttpServletRequest,
        @RequestBody createDto: CreateCommentRequestDto,
    ): CommentResponseDto? {
        return commentService.createComment(request.userId, createDto)
    }

    @PatchMapping("/community/comments/{commentId}")
    @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun patchComment(
        request: HttpServletRequest,
        @PathVariable commentId: Int,
        @RequestBody patchDto: PatchCommentRequestDto,
    ): CommentResponseDto {
        return commentService.patchComment(request.userId, commentId, patchDto)
    }

    @DeleteMapping("/community/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "댓글 삭제", description = "기존 댓글을 삭제합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun deleteComment(
        request: HttpServletRequest,
        @PathVariable commentId: Int,
    ) {
        commentService.deleteComment(request.userId, commentId)
    }

    @PostMapping("/community/comments/{commentId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "댓글 좋아요", description = "특정 댓글에 좋아요를 추가합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun createCommentLike(
        request: HttpServletRequest,
        @PathVariable commentId: Int,
    ): CommentResponseDto {
        return commentService.createOrUpdateCommentLike(request.userId, commentId, true)
    }

    // TODO: delete 방식으로 수정
    @PostMapping("/community/comments/{commentId}/unlike")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "댓글 좋아요 취소", description = "특정 댓글의 좋아요를 취소합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun createCommentUnlike(
        request: HttpServletRequest,
        @PathVariable commentId: Int,
    ): CommentResponseDto {
        return commentService.createOrUpdateCommentLike(request.userId, commentId, false)
    }

    @PostMapping("/community/comments/{commentId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "댓글 신고", description = "특정 댓글을 신고합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun createCommentReport(
        request: HttpServletRequest,
        @PathVariable commentId: Int,
        @RequestBody createDto: CreateCommentReportRequestDto,
    ): CommentsReportResponseDto {
        return commentService.createCommentReport(request.userId, commentId, createDto.reason)
    }
}
