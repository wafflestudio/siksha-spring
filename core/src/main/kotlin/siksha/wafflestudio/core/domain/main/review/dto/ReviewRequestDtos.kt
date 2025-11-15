package siksha.wafflestudio.core.domain.main.review.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

// TODO: 변수명 camelCase로 전환 (Jackson의 NamingStrategy 사용 불가)

/**
 * 원래 client가 snake_case로 요청을 보내면,
 * Jackson이 매핑을 통해 해당 값들을 내부 camelCase 변수에 넣어 주지만,
 * 현재 RequestBody 및 ModelAttribute에는 NamingStrategy가 적용되지 않아
 * 변수명을 snake_case로 사용 중.
 */

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
    val images: Any? = null,
)
