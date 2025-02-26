package siksha.wafflestudio.core.domain.post.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.post.data.Post
import java.time.LocalDateTime

@Repository
interface PostRepository: JpaRepository<Post, Int> {
    @EntityGraph(attributePaths = ["user"])
    @Query("SELECT p FROM post p WHERE p.board.id = :boardId ORDER BY p.createdAt DESC")
    fun findPageByBoardId(@Param("boardId") boardId: Int, pageable: Pageable): Page<Post>

    @EntityGraph(attributePaths = ["user"])
    @Query("SELECT p FROM post p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    fun findPageByUserId(@Param("userId") userId: Int, pageable: Pageable): Page<Post>

    // limit 5
    @EntityGraph(attributePaths = ["user"])
    @Query("SELECT p FROM post p WHERE p.createdAt >= :createdDays GROUP BY p.id HAVING (SELECT COUNT(DISTINCT pl.id) FROM post_like pl WHERE pl.post = p AND pl.isLiked = true) >= :minimumLikes ORDER BY (SELECT COUNT(DISTINCT pl.id) FROM post_like pl WHERE pl.post = p AND pl.isLiked = true) DESC, p.createdAt DESC")
    fun findTrending(@Param("minimumLikes") minimumLikes: Int, @Param("createdDays") createdDays: LocalDateTime, pageable: Pageable = PageRequest.of(0, 5)): Page<Post>

    @EntityGraph(attributePaths = ["user"])
    @Query("SELECT p FROM post p GROUP BY p.id HAVING (SELECT COUNT(DISTINCT pl.id) FROM post_like pl WHERE pl.post = p AND pl.isLiked = true) >= :minimumLikes ORDER BY (SELECT COUNT(DISTINCT pl.id) FROM post_like pl WHERE pl.post = p AND pl.isLiked = true) DESC, p.createdAt DESC")
    fun findBest(@Param("minimumLikes") minimumLikes: Int, pageable: Pageable = PageRequest.of(0, 5)): Page<Post>
}
