package siksha.wafflestudio.core.domain.community.comment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import siksha.wafflestudio.core.domain.community.comment.data.CommentLike

interface CommentLikeRepository : JpaRepository<CommentLike, Long> {
    @Query("SELECT cl FROM comment_like cl WHERE cl.comment.id IN :commentIds AND cl.isLiked = true")
    fun findByCommentIdInAndIsLiked(commentIds: List<Int>): List<CommentLike>

    @Query("SELECT cl FROM comment_like cl WHERE cl.comment.id = :commentId")
    fun findByCommentId(commentId: Int): List<CommentLike>

    @Query("DELETE FROM comment_like cl WHERE cl.comment.id = :commentId")
    fun deleteByCommentId(commentId: Int): Long

    @Query("SELECT count(*) FROM comment_like cl WHERE cl.comment.id = :commentId AND cl.isLiked = true")
    fun countCommentLikesByCommentIdAndIsLiked(commentId: Int): Long

    fun findCommentLikeByCommentIdAndUserId(
        commentId: Int,
        userId: Int,
    ): CommentLike?
}
