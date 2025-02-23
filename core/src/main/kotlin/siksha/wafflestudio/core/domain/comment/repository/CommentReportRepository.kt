package siksha.wafflestudio.core.domain.comment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.comment.data.CommentReport

@Repository
interface CommentReportRepository: JpaRepository<CommentReport, Long> {
    fun countCommentReportByCommentId(commentId: Long): Int
}
