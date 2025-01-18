package siksha.wafflestudio.core.service.comment

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import siksha.wafflestudio.core.domain.comment.service.CommentService

@SpringBootTest
class CommentServiceTest
@Autowired
constructor(
    private val service: CommentService,
) {
    @Test
    fun testComments() {
        val comments = service.getCommentsWithoutAuth(1, 1, 10).result
        assert(comments.isNotEmpty())
    }

    @Test
    fun testCommentLikes() {
        val commentLike = service.postCommentLike(1, 1, true)
        assert(commentLike.isLiked)

        val commentUnlike = service.postCommentLike(1, 1, false)
        assert(!commentUnlike.isLiked)
    }
}
