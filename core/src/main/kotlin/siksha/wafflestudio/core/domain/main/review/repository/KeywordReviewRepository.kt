package siksha.wafflestudio.core.domain.main.review.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.main.review.data.KeywordReview

@Repository
interface KeywordReviewRepository : JpaRepository<KeywordReview, Int> {
}
