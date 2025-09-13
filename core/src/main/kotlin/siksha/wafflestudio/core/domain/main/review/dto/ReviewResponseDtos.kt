package siksha.wafflestudio.core.domain.main.review.dto

import java.time.OffsetDateTime

data class ReviewResponse(
    val id: Int,
    val menuId: Int,
    val nameKr: String?,
    val nameEn: String?,
    val userId: Int,
    val score: Int,
    val comment: String?,
    val etc: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)

data class ReviewListResponse(
    val totalCount: Int,
    val hasNext: Boolean,
    val result: List<ReviewResponse>,
)

data class MyReviewsResponse(
    val totalCount: Int,
    val hasNext: Boolean,
    val result: List<RestaurantWithReviewListResponse>,
)

data class RestaurantWithReviewListResponse(
    val restaurantId: Int,
    val nameKr: String?,
    val nameEn: String?,
    val reviews: List<ReviewResponse>,
)

data class CommentRecommendationResponse(
    val comment: String,
)

data class ReviewScoreDistributionResponse(
    val dist: List<Int>,
)
