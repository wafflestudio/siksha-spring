package siksha.wafflestudio.core.domain.main.review.dto

import java.sql.Timestamp
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

    fun getLikeCount(): Int

    fun getIsLiked(): Int

    fun getCreatedAt(): Timestamp

    fun getUpdatedAt(): Timestamp
}
