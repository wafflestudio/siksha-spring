package siksha.wafflestudio.core.domain.main.review.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.review.data.ReviewLike

interface ReviewLikeRepository: JpaRepository<ReviewLike, Int> {
    fun deleteByUserIdAndReviewId(userId: Int, reviewId: Int): Int
}
