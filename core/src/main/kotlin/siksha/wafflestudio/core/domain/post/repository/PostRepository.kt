package siksha.wafflestudio.core.domain.post.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.post.data.Post

interface PostRepository: JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = ["user"])
    // FIXME: board 생성 후 수정
    // @Query("SELECT p FROM post p WHERE p.board.id = :boardId")
    @Query("SELECT p FROM post p")
    fun findPageByBoardId(@Param("boardId") boardId: Long, pageable: Pageable): Page<Post>
}
