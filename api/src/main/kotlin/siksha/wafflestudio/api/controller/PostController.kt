package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.application.post.dto.*
import siksha.wafflestudio.core.application.post.dto.GetPostsResponseDto
import siksha.wafflestudio.core.application.post.dto.PostCreateRequestDto
import siksha.wafflestudio.core.application.post.dto.PostResponseDto
import siksha.wafflestudio.core.application.post.PostApplicationService
import siksha.wafflestudio.core.application.post.dto.PostPatchRequestDto

@RestController
@RequestMapping("/community/posts")
@Validated
class PostController (
    private val postApplicationService: PostApplicationService,
) {
    @GetMapping("/web")
    fun getPostsWithoutAuth(
        @RequestParam(name = "board_id") boardId: Long,
        @RequestParam(name = "page", defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = "per_page", defaultValue = "10") @Min(1) perPage: Int,
    ): GetPostsResponseDto? {
        return postApplicationService.getPosts(boardId, page, perPage, null)
    }

    @GetMapping
    fun getPostsWithAuth(
        request: HttpServletRequest,
        @RequestParam(name = "board_id") boardId: Long,
        @RequestParam(name = "page", defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = "per_page", defaultValue = "10") @Min(1) perPage: Int,
    ): GetPostsResponseDto? {
        return postApplicationService.getPosts(boardId, page, perPage, request.userId)
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createPost(
        request: HttpServletRequest,
        @ModelAttribute createDto: PostCreateRequestDto,
    ): PostResponseDto? {
        return postApplicationService.createPost(request.userId, createDto)
    }

    @GetMapping("/me")
    fun getMyPosts(
        request: HttpServletRequest,
        @RequestParam(name = "page", defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = "per_page", defaultValue = "10") @Min(1) perPage: Int,
    ): GetPostsResponseDto? {
        return postApplicationService.getMyPosts(page, perPage, request.userId)
    }

    @GetMapping("/{post_id}/web")
    fun getPostWithoutAuth(
        @PathVariable("post_id") postId: Long,
    ): PostResponseDto? {
        return postApplicationService.getPost(postId, null)
    }

    @GetMapping("/{post_id}")
    fun getPostWithAuth(
        request: HttpServletRequest,
        @PathVariable("post_id") postId: Long,
    ): PostResponseDto? {
        return postApplicationService.getPost(postId, request.userId)
    }

    @PatchMapping("/{post_id}")
    fun patchPost(
        request: HttpServletRequest,
        @PathVariable("post_id") postId: Long,
        @ModelAttribute patchDto: PostPatchRequestDto,
    ): PostResponseDto? {
        return postApplicationService.patchPost(userId = request.userId, postId = postId, postPatchRequestDto = patchDto)
    }

    @DeleteMapping("/{post_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePost(
        request: HttpServletRequest,
        @PathVariable("post_id") postId: Long,
    ) {
        postApplicationService.deletePost(userId = request.userId, postId = postId)
    }

    @PostMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPostLike(
        request: HttpServletRequest,
        @PathVariable postId: Long,
    ): PostResponseDto {
        return postApplicationService.createOrUpdatePostLike(request.userId, postId, true)
    }

    //TODO: delete 방식으로 수정
    @PostMapping("/{postId}/unlike")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPostUnlike(
        request: HttpServletRequest,
        @PathVariable postId: Long,
    ): PostResponseDto {
        return postApplicationService.createOrUpdatePostLike(request.userId, postId, false)
    }

    @PostMapping("/{postId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPostReport(
        request: HttpServletRequest,
        @PathVariable postId: Long,
        @RequestBody createDto: CreatePostReportRequestDto,
    ): PostsReportResponseDto {
        return postApplicationService.createPostReport(request.userId, postId, createDto.reason)
    }
}
