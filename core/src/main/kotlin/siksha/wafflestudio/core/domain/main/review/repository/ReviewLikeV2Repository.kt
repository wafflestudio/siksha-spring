package siksha.wafflestudio.core.domain.main.review.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.review.data.ReviewLikeV2

interface ReviewLikeV2Repository : JpaRepository<ReviewLikeV2, Long> {
    fun existsByUserIdAndReviewId(
        userId: Int,
        reviewId: Long,
    ): Boolean

    fun countByReviewId(reviewId: Long): Int

    fun deleteByUserIdAndReviewId(
        userId: Int,
        reviewId: Long,
    ): Int
}
