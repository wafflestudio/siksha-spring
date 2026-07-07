package siksha.wafflestudio.core.domain.main.review.dto

import com.fasterxml.jackson.databind.JsonNode
import siksha.wafflestudio.core.domain.main.review.data.KeywordReviewV2
import siksha.wafflestudio.core.domain.main.review.data.ReviewV2
import siksha.wafflestudio.core.util.EtcUtils
import siksha.wafflestudio.core.util.KeywordReviewUtil
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class ReviewV2Response(
    val id: Long,
    val menuId: Long,
    val menuName: String,
    val restaurantId: Int,
    val restaurantName: String,
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
        fun from(review: ReviewV2Summary): ReviewV2Response =
            ReviewV2Response(
                id = review.getId(),
                menuId = review.getMenuId(),
                menuName = review.getMenuName(),
                restaurantId = review.getRestaurantId(),
                restaurantName = review.getRestaurantName(),
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
                createdAt = review.getCreatedAt().toInstant().atOffset(ZoneOffset.UTC),
                updatedAt = review.getUpdatedAt().toInstant().atOffset(ZoneOffset.UTC),
            )
    }
}

data class ReviewV2ListResponse(
    val totalCount: Int,
    val hasNext: Boolean,
    val result: List<ReviewV2Response>,
)

data class ReviewV2ScoreDistributionResponse(
    val dist: List<Int>,
)

data class KeywordReviewV2ScoreDistributionResponse(
    val tasteKeyword: String,
    val tasteCnt: Int = 0,
    val tasteTotal: Int = 0,
    val priceKeyword: String,
    val priceCnt: Int = 0,
    val priceTotal: Int = 0,
    val foodCompositionKeyword: String,
    val foodCompositionCnt: Int = 0,
    val foodCompositionTotal: Int = 0,
) {
    companion object {
        fun from(summary: KeywordReviewV2Summary): KeywordReviewV2ScoreDistributionResponse =
            KeywordReviewV2ScoreDistributionResponse(
                tasteKeyword = KeywordReviewUtil.getTasteKeyword(summary.getTasteKeyword()),
                tasteCnt = summary.getTasteCnt() ?: 0,
                tasteTotal = summary.getTasteTotal(),
                priceKeyword = KeywordReviewUtil.getPriceKeyword(summary.getPriceKeyword()),
                priceCnt = summary.getPriceCnt() ?: 0,
                priceTotal = summary.getPriceTotal(),
                foodCompositionKeyword = KeywordReviewUtil.getFoodCompositionKeyword(summary.getFoodCompositionKeyword()),
                foodCompositionCnt = summary.getFoodCompositionCnt() ?: 0,
                foodCompositionTotal = summary.getFoodCompositionTotal(),
            )
    }
}

data class MyReviewV2Response(
    val id: Long,
    val menuId: Long,
    val menuName: String,
    val restaurantId: Int,
    val restaurantName: String,
    val userId: Int,
    val score: Int,
    val comment: String?,
    val etc: JsonNode?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val keywordReviews: List<String?>,
    val isLiked: Boolean,
) {
    companion object {
        fun from(
            review: ReviewV2,
            keywordReview: KeywordReviewV2?,
            isLiked: Boolean,
        ): MyReviewV2Response =
            MyReviewV2Response(
                id = review.id,
                menuId = review.menu.id,
                menuName = review.menu.name,
                restaurantId = review.menu.restaurant.id,
                restaurantName = review.menu.restaurant.name,
                userId = review.user.id,
                score = review.score,
                comment = review.comment,
                etc = EtcUtils.convertEtc(review.etc),
                createdAt = review.createdAt,
                updatedAt = review.updatedAt,
                keywordReviews =
                    if (keywordReview == null) {
                        listOf(null, null, null)
                    } else {
                        listOf(
                            KeywordReviewUtil.getTasteKeyword(keywordReview.taste),
                            KeywordReviewUtil.getPriceKeyword(keywordReview.price),
                            KeywordReviewUtil.getFoodCompositionKeyword(keywordReview.foodComposition),
                        )
                    },
                isLiked = isLiked,
            )
    }
}

data class RestaurantWithReviewV2ListResponse(
    val restaurantId: Int,
    val restaurantName: String,
    val reviews: List<MyReviewV2Response>,
)

data class MyReviewsV2Response(
    val totalCount: Int,
    val hasNext: Boolean,
    val result: List<RestaurantWithReviewV2ListResponse>,
)
