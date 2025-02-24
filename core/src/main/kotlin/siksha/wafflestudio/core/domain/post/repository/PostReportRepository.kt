package siksha.wafflestudio.core.domain.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.post.data.PostReport
import siksha.wafflestudio.core.domain.user.data.User

@Repository
interface PostReportRepository: JpaRepository<PostReport, Int> {
    fun existsByPostIdAndReportingUser(postId: Int, reportingUser: User): Boolean

    fun countPostReportByPostId(postId: Int): Int
}
