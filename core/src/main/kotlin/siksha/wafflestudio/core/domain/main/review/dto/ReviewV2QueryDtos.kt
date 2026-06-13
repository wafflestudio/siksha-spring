package siksha.wafflestudio.core.domain.main.review.dto

import java.sql.Timestamp

interface ReviewV2Summary {
    fun getId(): Long

    fun getMenuId(): Long

    fun getMenuName(): String

    fun getRestaurantId(): Int

    fun getRestaurantName(): String

    fun getUserId(): Int

    fun getScore(): Int

    fun getComment(): String?

    fun getEtc(): String?

    fun getTaste(): Int?

    fun getPrice(): Int?

    fun getFoodComposition(): Int?

    fun getLikeCount(): Int

    fun getIsLiked(): Int

    fun getCreatedAt(): Timestamp

    fun getUpdatedAt(): Timestamp
}

interface KeywordReviewV2Summary {
    fun getTasteKeyword(): Int?

    fun getTasteCnt(): Int?

    fun getTasteTotal(): Int

    fun getPriceKeyword(): Int?

    fun getPriceCnt(): Int?

    fun getPriceTotal(): Int

    fun getFoodCompositionKeyword(): Int?

    fun getFoodCompositionCnt(): Int?

    fun getFoodCompositionTotal(): Int
}
