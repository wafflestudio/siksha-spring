package siksha.wafflestudio.core.domain.board.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.board.data.Board

@Repository
interface BoardRepository : JpaRepository<Board, Int> {
    fun existsByName(name: String): Boolean
}
