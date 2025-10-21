package siksha.wafflestudio.core.domain.main.review.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

// TODO: 변수명 camelCase로 전환 (Jackson의 NamingStrategy 사용 불가)
data class ReviewRequest(
    @field:NotNull
    val menu_id: Int,
    @field:Min(1)
    @field:Max(5)
    val score: Int,
    val comment: String? = null,
    val taste: String = "",
    val price: String = "",
    val food_composition: String = "",
)

data class ReviewWithImagesRequest(
    @field:NotNull
    val menu_id: Int,
    @field:Min(1)
    @field:Max(5)
    val score: Int,
    val comment: String? = null,
    val taste: String = "",
    val price: String = "",
    val food_composition: String = "",
    val images: List<MultipartFile>? = null,
)
