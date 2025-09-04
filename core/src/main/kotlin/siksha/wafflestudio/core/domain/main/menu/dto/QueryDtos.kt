package siksha.wafflestudio.core.domain.main.menu.dto

import java.sql.Timestamp
import java.time.LocalDate
import java.time.OffsetDateTime

interface MenuSummary {
    fun getId(): Int

    fun getRestaurantId(): Int

    fun getCode(): String

    fun getDate(): LocalDate

    fun getType(): String

    fun getNameKr(): String?

    fun getNameEn(): String?

    fun getPrice(): Int?

    fun getEtc(): String?

    fun getCreatedAt(): Timestamp

    fun getUpdatedAt(): Timestamp

    fun getScore(): Double?

    fun getReviewCnt(): Int
}

interface MenuPlainSummary {
    fun getId(): Int

    fun getRestaurantId(): Int

    fun getCode(): String
}

interface MenuLikeSummary {
    fun getId(): Int

    fun getLikeCnt(): Int

    fun getIsLiked(): Int
}

interface MenuLikeCount {
    fun getId(): Int

    fun getLikeCount(): Int
}
