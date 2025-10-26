package siksha.wafflestudio.core.domain.main.review.dto

import java.sql.Timestamp

interface ReviewSummary {
    fun getId(): Int

    fun getMenuId(): Int

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

interface KeywordReviewSummary {
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
