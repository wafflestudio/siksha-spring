package siksha.wafflestudio.core.domain.comment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import siksha.wafflestudio.core.domain.comment.data.CommentLike

interface CommentLikeRepository : JpaRepository<CommentLike, Long> {
    @Query("SELECT cl FROM comment_like cl WHERE cl.comment.id IN :commentIds")
    fun findByCommentIdIn(commentIds: List<Long>): List<CommentLike>

    @Query("SELECT cl FROM comment_like cl WHERE cl.comment.id = :commentId")
    fun findByCommentId(commentId: Long): List<CommentLike>

    @Query("DELETE FROM comment_like cl WHERE cl.comment.id = :commentId")
    fun deleteByCommentId(commentId: Long): Long

    @Query("SELECT count(*) FROM comment_like cl WHERE cl.comment.id = :commentId AND cl.isLiked = true")
    fun countCommentLikesByCommentIdAndLiked(commentId: Long): Long

    fun findCommentLikeByCommentIdAndUserId(commentId: Long, userId: Long): CommentLike?
}
