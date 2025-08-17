package siksha.wafflestudio.core.domain.community.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.community.post.data.PostReport

@Repository
interface PostReportRepository : JpaRepository<PostReport, Int> {
    fun countPostReportByPostId(postId: Int): Int
}
