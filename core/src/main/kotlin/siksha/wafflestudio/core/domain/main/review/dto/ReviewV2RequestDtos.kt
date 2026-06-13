package siksha.wafflestudio.core.domain.main.review.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class ReviewV2Request(
    @field:NotNull
    val menu_id: Long,
    @field:Min(1)
    @field:Max(5)
    val score: Int,
    val comment: String? = null,
    val taste: String = "",
    val price: String = "",
    val food_composition: String = "",
)

data class ReviewV2WithImagesRequest(
    @field:NotNull
    val menu_id: Long,
    @field:Min(1)
    @field:Max(5)
    val score: Int,
    val comment: String? = null,
    val taste: String = "",
    val price: String = "",
    val food_composition: String = "",
    val images: Any? = null,
)
