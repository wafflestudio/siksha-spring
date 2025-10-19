package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Validator
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.common.exception.InvalidPostFormException
import siksha.wafflestudio.core.domain.community.post.dto.CreatePostReportRequestDto
import siksha.wafflestudio.core.domain.community.post.dto.PaginatedPostsResponseDto
import siksha.wafflestudio.core.domain.community.post.dto.PostCreateRequestDto
import siksha.wafflestudio.core.domain.community.post.dto.PostPatchRequestDto
import siksha.wafflestudio.core.domain.community.post.dto.PostResponseDto
import siksha.wafflestudio.core.domain.community.post.dto.PostsReportResponseDto
import siksha.wafflestudio.core.domain.community.post.dto.PostsResponseDto
import siksha.wafflestudio.core.domain.community.post.service.PostService

@RestController
@RequestMapping("/community/posts")
@Validated
@Tag(name = "Posts", description = "커뮤니티 게시글 관리 엔드포인트")
class PostController(
    private val postService: PostService,
    private val validator: Validator,
) {
    @GetMapping("/web")
    @Operation(summary = "게시글 목록 조회 (비로그인)", description = "특정 게시판의 게시글 목록을 조회합니다 (비로그인)")
    fun getPostsWithoutAuth(
        @RequestParam(name = "board_id") boardId: Int,
        @RequestParam(name = "page", defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = "per_page", defaultValue = "10") @Min(1) perPage: Int,
    ): PaginatedPostsResponseDto? {
        return postService.getPosts(boardId, page, perPage, null)
    }

    @GetMapping
    @Operation(summary = "게시글 목록 조회 (로그인)", description = "특정 게시판의 게시글 목록을 조회합니다 (로그인)")
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다")
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "내 게시글 조회", description = "인증된 사용자가 작성한 게시글 목록을 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun getMyPosts(
        request: HttpServletRequest,
        @RequestParam(name = "page", defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = "per_page", defaultValue = "10") @Min(1) perPage: Int,
    ): PaginatedPostsResponseDto? {
        return postService.getMyPosts(page, perPage, request.userId)
    }

    @GetMapping("/{post_id}/web")
    @Operation(summary = "게시글 상세 조회 (비로그인)", description = "특정 게시글의 상세 정보를 조회합니다 (비로그인)")
    fun getPostWithoutAuth(
        @PathVariable("post_id") postId: Int,
    ): PostResponseDto? {
        return postService.getPost(postId, null)
    }

    @GetMapping("/{post_id}")
    @Operation(summary = "게시글 상세 조회 (로그인)", description = "특정 게시글의 상세 정보를 조회합니다 (로그인)")
    @SecurityRequirement(name = "bearerAuth")
    fun getPostWithAuth(
        request: HttpServletRequest,
        @PathVariable("post_id") postId: Int,
    ): PostResponseDto? {
        return postService.getPost(postId, request.userId)
    }

    @PatchMapping("/{post_id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun patchPost(
        request: HttpServletRequest,
        @PathVariable("post_id") postId: Int,
        @ModelAttribute requestDto: PostPatchRequestDto,
    ): PostResponseDto? {
        val patchDto = requestDto.copy(anonymous = requestDto.anonymous ?: false)
        val violations = validator.validate(patchDto)
        if (violations.isNotEmpty()) throw InvalidPostFormException(violations.first().message)
        return postService.patchPost(userId = request.userId, postId = postId, postPatchRequestDto = patchDto)
    }

    @DeleteMapping("/{post_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "게시글 삭제", description = "기존 게시글을 삭제합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun deletePost(
        request: HttpServletRequest,
        @PathVariable("post_id") postId: Int,
    ) {
        postService.deletePost(userId = request.userId, postId = postId)
    }

    @PostMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "게시글 좋아요", description = "특정 게시글에 좋아요를 추가합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun createPostLike(
        request: HttpServletRequest,
        @PathVariable postId: Int,
    ): PostResponseDto {
        return postService.createOrUpdatePostLike(request.userId, postId, true)
    }

    // TODO: delete 방식으로 수정
    @PostMapping("/{postId}/unlike")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "게시글 좋아요 취소", description = "특정 게시글의 좋아요를 취소합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun createPostUnlike(
        request: HttpServletRequest,
        @PathVariable postId: Int,
    ): PostResponseDto {
        return postService.createOrUpdatePostLike(request.userId, postId, false)
    }

    @PostMapping("/{postId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "게시글 신고", description = "특정 게시글을 신고합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun createPostReport(
        request: HttpServletRequest,
        @PathVariable postId: Int,
        @RequestBody createDto: CreatePostReportRequestDto,
    ): PostsReportResponseDto {
        return postService.createPostReport(request.userId, postId, createDto.reason)
    }

    @GetMapping("/popular/trending")
    @Operation(summary = "인기 게시글 조회 (트렌딩, 로그인)", description = "최근 인기있는 게시글을 조회합니다 (로그인)")
    @SecurityRequirement(name = "bearerAuth")
    fun getTrendingPosts(
        request: HttpServletRequest,
        @RequestParam(name = "likes", defaultValue = "10") @Min(1) likes: Int,
        @RequestParam(name = "created_before", defaultValue = "7") @Min(1) createdBefore: Int,
    ): PostsResponseDto? {
        return postService.getTrendingPosts(likes = likes, createdBefore = createdBefore, userId = request.userId)
    }

    @GetMapping("/popular/trending/web")
    @Operation(summary = "인기 게시글 조회 (트렌딩, 비로그인)", description = "최근 인기있는 게시글을 조회합니다 (비로그인)")
    fun getTrendingPostsWithoutAuth(
        @RequestParam(name = "likes", defaultValue = "10") @Min(1) likes: Int,
        @RequestParam(name = "created_before", defaultValue = "7") @Min(1) createdBefore: Int,
    ): PostsResponseDto? {
        return postService.getTrendingPosts(likes = likes, createdBefore = createdBefore, userId = null)
    }

    @GetMapping("/popular/best")
    @Operation(summary = "인기 게시글 조회 (베스트, 로그인)", description = "베스트 게시글을 조회합니다 (로그인)")
    @SecurityRequirement(name = "bearerAuth")
    fun getBestPosts(
        request: HttpServletRequest,
        @RequestParam(name = "likes", defaultValue = "10") @Min(1) likes: Int,
    ): PostsResponseDto? {
        return postService.getBestPosts(likes = likes, userId = request.userId)
    }

    @GetMapping("/popular/best/web")
    @Operation(summary = "인기 게시글 조회 (베스트, 비로그인)", description = "베스트 게시글을 조회합니다 (비로그인)")
    fun getBestPostsWithoutAuth(
        @RequestParam(name = "likes", defaultValue = "10") @Min(1) likes: Int,
    ): PostsResponseDto? {
        return postService.getBestPosts(likes = likes, userId = null)
    }
}
