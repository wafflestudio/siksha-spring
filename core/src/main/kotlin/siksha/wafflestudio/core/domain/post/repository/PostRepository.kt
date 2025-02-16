package siksha.wafflestudio.core.domain.post.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.post.data.Post

@Repository
interface PostRepository: JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = ["user"])
    @Query("SELECT p FROM post p WHERE p.board.id = :boardId ORDER BY p.createdAt DESC")
    fun findPageByBoardId(@Param("boardId") boardId: Long, pageable: Pageable): Page<Post>

    @EntityGraph(attributePaths = ["user"])
    @Query("SELECT p FROM post p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    fun findPageByUserId(@Param("userId") userId: Long, pageable: Pageable): Page<Post>
}
