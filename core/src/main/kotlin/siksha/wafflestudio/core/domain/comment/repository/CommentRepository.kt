package siksha.wafflestudio.core.domain.comment.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import siksha.wafflestudio.core.domain.comment.data.Comment

interface CommentRepository : JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = ["user"])
    @Query("SELECT c FROM comment c")
    fun findPageByPostId(pageable: Pageable): Page<Comment>
}
