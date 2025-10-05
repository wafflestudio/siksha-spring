package siksha.wafflestudio.core.domain.main.review.dto

import com.fasterxml.jackson.databind.JsonNode
import siksha.wafflestudio.core.util.EtcUtils
import siksha.wafflestudio.core.util.KeywordReviewUtil
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class ReviewResponse(
    val id: Int,
    val menuId: Int,
    val nameKr: String?,
    val nameEn: String?,
    val userId: Int,
    val score: Int,
    val comment: String?,
    val etc: JsonNode,
    val keywordReviews: List<String?>,
    val likeCount: Int,
    val isLiked: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun from(review: ReviewSummary): ReviewResponse {
            return ReviewResponse(
                id = review.getId(),
                menuId = review.getMenuId(),
                userId = review.getUserId(),
                score = review.getScore(),
                comment = review.getComment(),
                etc = EtcUtils.convertEtc(review.getEtc()),
                keywordReviews =
                    listOf(
                        KeywordReviewUtil.getTasteKeyword(review.getTaste()),
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

data class KeywordScoreDistributionResponse(
    val tasteKeyword: String = "맛",
    val tasteCnt: Int = 0,
    val priceKeyword: String = "가격",
    val priceCnt: Int = 0,
    val foodCompositionKeyword: String = "음식구성",
    val foodCompositionCnt: Int = 0,
) {
    companion object {
        fun from(keywordReviewSummary: KeywordReviewSummary): KeywordScoreDistributionResponse {
            return KeywordScoreDistributionResponse(
                tasteKeyword =
                    KeywordReviewUtil.getTasteKeyword(
                        keywordReviewSummary.getTasteKeyword(),
                    ),
                tasteCnt = keywordReviewSummary.getTasteCnt(),
                priceKeyword =
                    KeywordReviewUtil.getPriceKeyword(
                        keywordReviewSummary.getPriceKeyword(),
                    ),
                priceCnt = keywordReviewSummary.getPriceCnt(),
                foodCompositionKeyword =
                    KeywordReviewUtil.getFoodCompositionKeyword(
                        keywordReviewSummary.getFoodCompositionKeyword(),
                    ),
                foodCompositionCnt = keywordReviewSummary.getFoodCompositionCnt(),
            )
        }
    }
}

data class MyReviewResponse(
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
    val keywordReviews: List<String?>,
)

data class RestaurantWithReviewListResponse(
    val restaurantId: Int,
    val nameKr: String?,
    val nameEn: String?,
    val reviews: List<MyReviewResponse>,
)

data class MyReviewsResponse(
    val totalCount: Int,
    val hasNext: Boolean,
    val result: List<RestaurantWithReviewListResponse>,
)
