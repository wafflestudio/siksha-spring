package siksha.wafflestudio.core.domain.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.post.data.PostReport

interface PostReportRepository: JpaRepository<PostReport, Long> {
}
