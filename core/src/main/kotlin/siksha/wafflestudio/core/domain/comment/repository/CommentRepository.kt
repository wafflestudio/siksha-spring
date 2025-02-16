package siksha.wafflestudio.core.domain.comment.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.comment.data.Comment

interface CommentRepository : JpaRepository<Comment, Int> {
    @EntityGraph(attributePaths = ["user", "post"])
    @Query("SELECT c FROM comment c WHERE c.post.id = :postId")
    fun findPageByPostId(@Param("postId") postId: Int, pageable: Pageable): Page<Comment>

    @Query("SELECT c FROM comment c WHERE c.post.id IN :postIds")
    fun findByPostIdIn(@Param("postIds") postIds: List<Int>): List<Comment>

    @Query("SELECT COUNT(c) FROM comment c WHERE c.post.id = :postId")
    fun countByPostId(@Param("postId") postId: Int): Int

    fun countCommentsByPostId(postId: Int): Int
}
