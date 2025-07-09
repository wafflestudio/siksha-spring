package siksha.wafflestudio.core.domain.main.review.dto

import java.time.LocalDate
import java.time.OffsetDateTime

data class ReviewResponse(
    val id: Int,
    val menuId: Int,
    val userId: Int,
    val score: Int,
    val comment: String?,
    val etc: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

data class ReviewListResponse(
    val totalCount: Int,
    val hasNext: Boolean,
    val result: List<ReviewResponse>
)

data class CommentRecommendationResponse(
    val comment: String
)

data class ReviewScoreDistributionResponse(
    val dist: List<Int>
)

data class MenuDetailsDto(
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val id: Int,
    val restaurantId: Int,
    val code: String,
    val date: LocalDate,
    val type: String,
    val nameKr: String?,
    val nameEn: String?,
    val price: Int?,
    val etc: List<String>?,
    val score: Double?,
    val reviewCnt: Int,
    val isLiked: Boolean,
    val likeCnt: Int
)

