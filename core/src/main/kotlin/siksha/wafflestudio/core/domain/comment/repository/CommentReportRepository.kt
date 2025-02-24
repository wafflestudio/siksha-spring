package siksha.wafflestudio.core.domain.comment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.comment.data.CommentReport
import siksha.wafflestudio.core.domain.user.data.User

@Repository
interface CommentReportRepository: JpaRepository<CommentReport, Int> {
    fun existsByCommentIdAndReportingUser(commentId: Int, reportingUser: User): Boolean

    fun countCommentReportByCommentId(commentId: Int): Int
}
