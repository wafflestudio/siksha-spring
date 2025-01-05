package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.application.post.dto.GetPostsResponseDto
import siksha.wafflestudio.core.application.post.dto.PostCreateDto
import siksha.wafflestudio.core.application.post.dto.PostResponseDto
import siksha.wafflestudio.core.application.post.PostApplicationService
import siksha.wafflestudio.core.infrastructure.s3.S3Service
import java.net.URL

@RestController
@RequestMapping("/community/posts")
@Validated
class PostController (
    private val postApplicationService: PostApplicationService,
    private val s3Service: S3Service, // FIXME
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

    // FIXME: auth 해결 후 /web 삭제
    @PostMapping("/web", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createPost(
        request: HttpServletRequest,
        @ModelAttribute post: PostCreateDto,
    ): PostResponseDto? {
        return postApplicationService.createPost(119, post) // FIXME: request.userId로 수정
    }
}
