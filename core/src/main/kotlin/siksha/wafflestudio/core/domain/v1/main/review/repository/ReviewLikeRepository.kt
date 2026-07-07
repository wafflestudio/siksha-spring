package siksha.wafflestudio.core.domain.v1.main.review.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.v1.main.review.data.ReviewLike

interface ReviewLikeRepository : JpaRepository<ReviewLike, Int> {
    fun existsByUserIdAndReviewId(
        userId: Int,
        reviewId: Int,
    ): Boolean

    fun deleteByUserIdAndReviewId(
        userId: Int,
        reviewId: Int,
    ): Int
}
