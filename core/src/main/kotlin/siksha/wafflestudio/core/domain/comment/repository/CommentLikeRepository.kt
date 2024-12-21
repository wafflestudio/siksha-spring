package siksha.wafflestudio.core.domain.comment.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import siksha.wafflestudio.core.domain.comment.data.CommentLike

interface CommentLikeRepository : JpaRepository<CommentLike, Long> {
    @Query("SELECT cl FROM comment_like cl WHERE cl.comment.id IN :commentIds")
    fun findByCommentIdIn(commentIds: List<Long>): List<CommentLike>
}
