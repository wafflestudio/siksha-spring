package siksha.wafflestudio.core.domain.main.review.dto

import siksha.wafflestudio.core.util.KeywordReviewUtil
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class ReviewResponse(
    val id: Int,
    val menuId: Int,
    val userId: Int,
    val score: Int,
    val comment: String?,
    val etc: String?,
    val keywordReviews: List<String?>,
    val likeCount: Int,
    val isLiked: Boolean,
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
                likeCount = review.getLikeCount(),
                isLiked = review.getIsLiked() == 1,
                createdAt = review.getCreatedAt().toLocalDateTime().atOffset(ZoneOffset.UTC),
                updatedAt = review.getUpdatedAt().toLocalDateTime().atOffset(ZoneOffset.UTC),
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
