package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.Min
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.core.domain.post.dto.GetPostsResponseDto
import siksha.wafflestudio.core.domain.post.service.PostService

@RestController
@RequestMapping("/community/posts")
@Validated
class PostController (
    private val postService: PostService,
) {
    @GetMapping("/web")
    fun getPostsWithoutAuth(
        @RequestParam(name = "board_id") boardId: Long,
        @RequestParam(name = "page", defaultValue = "1") @Min(1) page: Int,
        @RequestParam(name = "per_page", defaultValue = "10") @Min(1) perPage: Int,
    ): GetPostsResponseDto? {
        return postService.getPostsWithoutAuth(boardId, page, perPage)
    }
}
