package siksha.wafflestudio.core.repository

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import siksha.wafflestudio.core.domain.comment.repository.CommentRepository
import siksha.wafflestudio.core.domain.restaurant.data.RestaurantRepository

@SpringBootTest
class CommentTest
    @Autowired
    constructor(
        private val repository: CommentRepository,
    ) {
        @Test
        fun testComment() {
            val comments = repository.findAll()
            assert(comments.isNotEmpty())
        }
    }
