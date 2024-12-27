package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.core.domain.post.dto.GetPostsResponseDto
import siksha.wafflestudio.core.domain.post.service.PostService

@RestController
class PostController (
    private val postService: PostService,
) {
    @GetMapping("/community/posts/web")
    fun getPostsWithoutAuth(
        request: HttpServletRequest,
        @RequestParam(name = "board_id") boardId: Long,
        @RequestParam(name = "page") page: Int,
        @RequestParam(name = "per_page") perPage: Int,
    ): GetPostsResponseDto? {
        return postService.getPostsWithoutAuth(boardId, page, perPage)
    }
}
