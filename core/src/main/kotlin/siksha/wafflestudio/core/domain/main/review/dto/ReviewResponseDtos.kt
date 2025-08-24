package siksha.wafflestudio.core.domain.main.review.dto

import siksha.wafflestudio.core.util.KeywordReviewUtil
import java.time.OffsetDateTime

data class ReviewResponse(
    val id: Int,
    val menuId: Int,
    val userId: Int,
    val score: Int,
    val comment: String?,
    val etc: String?,
    val keywordReviews: List<String?>,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun from(
            review: ReviewSummary,
        ): ReviewResponse {
            return ReviewResponse(
                id = review.getId(),
                menuId = review.getMenuId(),
                userId = review.getUserId(),
                score = review.getScore(),
                comment = review.getComment(),
                etc = review.getEtc(),
                keywordReviews = listOf(
                    KeywordReviewUtil.getFlavorKeyword(review.getFlavor()),
                    KeywordReviewUtil.getPriceKeyword(review.getPrice()),
                    KeywordReviewUtil.getFoodCompositionKeyword(review.getFoodComposition()),
                ),
                createdAt = review.getCreatedAt(),
                updatedAt = review.getUpdatedAt(),
            )
        }
    }
}

data class ReviewListResponse(
    val totalCount: Int,
    val hasNext: Boolean,
    val result: List<ReviewResponse>,
)

data class CommentRecommendationResponse(
    val comment: String,
)

data class ReviewScoreDistributionResponse(
    val dist: List<Int>,
)
