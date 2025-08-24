package siksha.wafflestudio.core.domain.main.review.dto

import java.time.OffsetDateTime

interface ReviewSummary {
    fun getId(): Int

    fun getMenuId(): Int

    fun getUserId(): Int

    fun getScore(): Int

    fun getComment(): String?

    fun getEtc(): String?

    fun getFlavor(): Int?

    fun getPrice(): Int?

    fun getFoodComposition(): Int?

    fun getCreatedAt(): OffsetDateTime

    fun getUpdatedAt(): OffsetDateTime
}
