package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.post.dto.GetPostsResponseDto
import siksha.wafflestudio.core.domain.post.dto.PostCreateDto
import siksha.wafflestudio.core.domain.post.dto.PostResponseDto
import siksha.wafflestudio.core.domain.post.service.PostApplicationService

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

    // TODO impl this
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    fun createPost(
//        request: HttpServletRequest,
//        @RequestBody post: PostCreateDto,
//    ): PostResponseDto? {}
}
