package siksha.wafflestudio.core.domain.review.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.review.data.Review

@Repository
interface ReviewRepository : JpaRepository<Review, Int>
