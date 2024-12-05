package siksha.wafflestudio.core.domain.comment.service

import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.comment.repository.CommentRepository

@Service
class CommentService(
    private val commentRepository: CommentRepository,
){
    fun getComments(userId: Long) {
        val comments = commentRepository.findByUserId(userId)
        println(comments.map { it.content })
    }
}
