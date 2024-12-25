package siksha.wafflestudio.core.service

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
        val comments = service.getCommentsWithoutAuth(0, 10).result
        assert(comments.isNotEmpty())
    }
}
