package siksha.wafflestudio.core.domain.main.review.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

data class ReviewRequest(
    @field:NotNull
    val menuId: Int,

    @field:Min(1)
    @field:Max(5)
    val score: Int,

    val comment: String? = null
)

data class ReviewWithImagesRequest(
    @field:NotNull
    val menuId: Int,

    @field:Min(1)
    @field:Max(5)
    val score: Int,

    val comment: String? = null,

    val images: List<MultipartFile>? = null
)
