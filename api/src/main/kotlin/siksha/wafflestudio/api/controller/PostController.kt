package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Validator
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.common.exception.InvalidPostFormException
import siksha.wafflestudio.core.domain.post.dto.*
import siksha.wafflestudio.core.domain.post.service.PostService

@RestController
@RequestMapping("/community/posts")
@Validated
class PostController(
    private val postService: PostService,
    private val validator: Validator,
) {
    @GetMapping("/web")
    fun getPostsWithoutAuth(
        @RequestParam(name = "board_id") boardId: Int,
        @RequestParam(name = "page", defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = "per_page", defaultValue = "10") @Min(1) perPage: Int,
    ): PaginatedPostsResponseDto? {
        return postService.getPosts(boardId, page, perPage, null)
    }

    @GetMapping
    fun getPostsWithAuth(
        request: HttpServletRequest,
        @RequestParam(name = "board_id") boardId: Int,
        @RequestParam(name = "page", defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = "per_page", defaultValue = "10") @Min(1) perPage: Int,
    ): PaginatedPostsResponseDto? {
        return postService.getPosts(boardId, page, perPage, request.userId)
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createPost(
        request: HttpServletRequest,
        @RequestParam("board_id") boardId: Int,
        @RequestPart("title") title: String,
        @RequestPart("content") content: String,
        @RequestPart("anonymous", required = false) anonymous: Boolean? = false,
        @RequestPart("images", required = false) images: List<MultipartFile>?,
    ): PostResponseDto? {
        val createDto =
            PostCreateRequestDto(
                boardId = boardId,
                title = title,
                content = content,
                anonymous = anonymous ?: false,
                images = images,
            )
        val violations = validator.validate(createDto)
        if (violations.isNotEmpty()) throw InvalidPostFormException(violations.first().message)
        return postService.createPost(request.userId, createDto)
    }

    @GetMapping("/me")
    fun getMyPosts(
        request: HttpServletRequest,
        @RequestParam(name = "page", defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = "per_page", defaultValue = "10") @Min(1) perPage: Int,
    ): PaginatedPostsResponseDto? {
        return postService.getMyPosts(page, perPage, request.userId)
    }

    @GetMapping("/{post_id}/web")
    fun getPostWithoutAuth(
        @PathVariable("post_id") postId: Int,
    ): PostResponseDto? {
        return postService.getPost(postId, null)
    }

    @GetMapping("/{post_id}")
    fun getPostWithAuth(
        request: HttpServletRequest,
        @PathVariable("post_id") postId: Int,
    ): PostResponseDto? {
        return postService.getPost(postId, request.userId)
    }

    @PatchMapping("/{post_id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun patchPost(
        request: HttpServletRequest,
        @PathVariable("post_id") postId: Int,
        @RequestPart("title") title: String?,
        @RequestPart("content") content: String?,
        @RequestPart("anonymous", required = false) anonymous: Boolean? = false,
        @RequestPart("images", required = false) images: List<MultipartFile>?,
    ): PostResponseDto? {
        val patchDto =
            PostPatchRequestDto(
                title = title,
                content = content,
                anonymous = anonymous ?: false,
                images = images,
            )
        val violations = validator.validate(patchDto)
        if (violations.isNotEmpty()) throw InvalidPostFormException(violations.first().message)
        return postService.patchPost(userId = request.userId, postId = postId, postPatchRequestDto = patchDto)
    }

    @DeleteMapping("/{post_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePost(
        request: HttpServletRequest,
        @PathVariable("post_id") postId: Int,
    ) {
        postService.deletePost(userId = request.userId, postId = postId)
    }

    @PostMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPostLike(
        request: HttpServletRequest,
        @PathVariable postId: Int,
    ): PostResponseDto {
        return postService.createOrUpdatePostLike(request.userId, postId, true)
    }

    // TODO: delete 방식으로 수정
    @PostMapping("/{postId}/unlike")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPostUnlike(
        request: HttpServletRequest,
        @PathVariable postId: Int,
    ): PostResponseDto {
        return postService.createOrUpdatePostLike(request.userId, postId, false)
    }

    @PostMapping("/{postId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPostReport(
        request: HttpServletRequest,
        @PathVariable postId: Int,
        @RequestBody createDto: CreatePostReportRequestDto,
    ): PostsReportResponseDto {
        return postService.createPostReport(request.userId, postId, createDto.reason)
    }

    @GetMapping("/popular/trending")
    fun getTrendingPosts(
        request: HttpServletRequest,
        @RequestParam(name = "likes", defaultValue = "10") @Min(1) likes: Int,
        @RequestParam(name = "created_before", defaultValue = "7") @Min(1) createdBefore: Int,
    ): PostsResponseDto? {
        return postService.getTrendingPosts(likes = likes, createdBefore = createdBefore, userId = request.userId)
    }

    @GetMapping("/popular/trending/web")
    fun getTrendingPostsWithoutAuth(
        @RequestParam(name = "likes", defaultValue = "10") @Min(1) likes: Int,
        @RequestParam(name = "created_before", defaultValue = "7") @Min(1) createdBefore: Int,
    ): PostsResponseDto? {
        return postService.getTrendingPosts(likes = likes, createdBefore = createdBefore, userId = null)
    }

    @GetMapping("/popular/best")
    fun getBestPosts(
        request: HttpServletRequest,
        @RequestParam(name = "likes", defaultValue = "10") @Min(1) likes: Int,
    ): PostsResponseDto? {
        return postService.getBestPosts(likes = likes, userId = request.userId)
    }

    @GetMapping("/popular/best/web")
    fun getBestPostsWithoutAuth(
        @RequestParam(name = "likes", defaultValue = "10") @Min(1) likes: Int,
    ): PostsResponseDto? {
        return postService.getBestPosts(likes = likes, userId = null)
    }
}
