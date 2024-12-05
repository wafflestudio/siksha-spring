package siksha.wafflestudio.core.domain.comment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import siksha.wafflestudio.core.domain.comment.data.Comment

interface CommentRepository : JpaRepository<Comment, Long> {
    @Query("SELECT c FROM comment c JOIN FETCH c.user WHERE c.user.id = :userId")
    fun findByUserId(userId: Long): List<Comment>
}
