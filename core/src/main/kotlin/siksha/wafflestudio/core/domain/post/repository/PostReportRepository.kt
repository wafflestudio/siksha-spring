package siksha.wafflestudio.core.domain.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.post.data.PostReport
import siksha.wafflestudio.core.domain.user.data.User

@Repository
interface PostReportRepository: JpaRepository<PostReport, Long> {
    fun existsByPostIdAndReportingUser(postId: Long, reportingUser: User): Boolean
}
