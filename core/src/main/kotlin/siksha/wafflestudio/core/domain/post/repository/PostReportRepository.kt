package siksha.wafflestudio.core.domain.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.post.data.PostReport
import siksha.wafflestudio.core.domain.user.data.User

@Repository
<<<<<<< HEAD
interface PostReportRepository: JpaRepository<PostReport, Long> {
    fun existsByPostIdAndReportingUser(postId: Long, reportingUser: User): Boolean

    fun countPostReportByPostId(postId: Long): Int
=======
interface PostReportRepository: JpaRepository<PostReport, Int> {
>>>>>>> d75df59 (feat: flyway 세팅, id 값 long -> int)
}
