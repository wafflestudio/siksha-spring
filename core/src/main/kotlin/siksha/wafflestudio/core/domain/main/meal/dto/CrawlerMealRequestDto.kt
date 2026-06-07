package siksha.wafflestudio.core.domain.main.meal.dto

import com.fasterxml.jackson.annotation.JsonProperty
import siksha.wafflestudio.core.domain.main.meal.data.MealType
import java.time.LocalDate

data class CrawlerMealRequestDto(
    @JsonProperty("buildingNumber") val buildingNumber: String,
    @JsonProperty("buildingName") val buildingName: String? = null,
    val restaurant: String,
    val corner: String? = null,
    val date: LocalDate,
    val type: MealType,
    val meals: List<MealItem>,
) {
    data class MealItem(
        val price: Int? = null,
        @JsonProperty("noMeat") val noMeat: Boolean = false,
        val menus: List<String>,
    )
}
