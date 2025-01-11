package siksha.wafflestudio.core.domain.comment.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.comment.data.Comment

interface CommentRepository : JpaRepository<Comment, Long> {
<<<<<<< HEAD
    @EntityGraph(attributePaths = ["user", "post"])
    @Query("SELECT c FROM comment c WHERE c.post.id = :postId")
    fun findPageByPostId(@Param("postId") postId: Long, pageable: Pageable): Page<Comment>

    @Query("SELECT c FROM comment c WHERE c.post.id IN :postIds")
    fun findByPostIdIn(@Param("postIds") postIds: List<Long>): List<Comment>
=======
    @EntityGraph(attributePaths = ["user"])
    @Query("SELECT c FROM comment c WHERE c.postId =:postId")
    fun findPageByPostId(postId: Long, pageable: Pageable): Page<Comment>
>>>>>>> f6f24d3 (댓글 api들 추가)
}
