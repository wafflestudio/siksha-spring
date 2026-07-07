package siksha.wafflestudio.core.domain.main.menu.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDate

interface MenuV2MealListRow {
    fun getMealMenuId(): Long

    fun getMealId(): Long

    fun getMenuId(): Long

    fun getOriginalName(): String

    fun getRestaurantId(): Int

    fun getDate(): LocalDate

    fun getType(): String

    fun getPrice(): Int?

    fun getNoMeat(): Boolean

    fun getScore(): Double?

    fun getReviewCnt(): Int

    fun getLikeCnt(): Int

    fun getIsLiked(): Int
}

interface MenuV2DetailRow {
    fun getMenuId(): Long

    fun getMenuName(): String

    fun getRestaurantId(): Int

    fun getRestaurantName(): String

    fun getBuildingId(): Int

    fun getBuildingNumber(): String

    fun getBuildingName(): String?

    fun getScore(): Double?

    fun getReviewCnt(): Int

    fun getLikeCnt(): Int

    fun getIsLiked(): Int
}

interface MenuV2MealContextRow {
    fun getDate(): LocalDate

    fun getType(): String

    fun getPrice(): Int?

    fun getNoMeat(): Boolean

    fun getOriginalName(): String
}

interface MenuV2LikedMenuRow {
    fun getMenuId(): Long

    fun getMenuName(): String

    fun getRestaurantId(): Int

    fun getScore(): Double?

    fun getReviewCnt(): Int

    fun getLikeCnt(): Int

    fun getAlarm(): Int
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2MealListMenuDto(
    val menuId: Long,
    val menuName: String,
    val score: Double?,
    val reviewCnt: Int,
    val likeCnt: Int,
    val isLiked: Boolean,
) {
    companion object {
        fun from(row: MenuV2MealListRow): MenuV2MealListMenuDto =
            MenuV2MealListMenuDto(
                menuId = row.getMenuId(),
                menuName = row.getOriginalName(),
                score = row.getScore(),
                reviewCnt = row.getReviewCnt(),
                likeCnt = row.getLikeCnt(),
                isLiked = row.getIsLiked() == 1,
            )
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2MealListMealDto(
    val price: Int?,
    val noMeat: Boolean,
    val menus: List<MenuV2MealListMenuDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2MealListRestaurantDto(
    val id: Int,
    val restaurantName: String,
    val meals: List<MenuV2MealListMealDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2MealListBuildingDto(
    val buildingNumber: String,
    val buildingName: String?,
    val restaurants: List<MenuV2MealListRestaurantDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2MealListResponseDto(
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val date: LocalDate,
    val dateType: String,
    val type: String,
    val buildings: List<MenuV2MealListBuildingDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2LikedMenuDto(
    val menuId: Long,
    val menuName: String,
    val score: Double?,
    val reviewCnt: Int,
    val likeCnt: Int,
    val isLiked: Boolean = true,
    val alarm: Boolean,
) {
    companion object {
        fun from(row: MenuV2LikedMenuRow): MenuV2LikedMenuDto =
            MenuV2LikedMenuDto(
                menuId = row.getMenuId(),
                menuName = row.getMenuName(),
                score = row.getScore(),
                reviewCnt = row.getReviewCnt(),
                likeCnt = row.getLikeCnt(),
                alarm = row.getAlarm() == 1,
            )
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2LikedRestaurantDto(
    val id: Int,
    val restaurantName: String,
    val menus: List<MenuV2LikedMenuDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2LikedBuildingDto(
    val buildingNumber: String,
    val buildingName: String?,
    val restaurants: List<MenuV2LikedRestaurantDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2LikedListResponseDto(
    val buildings: List<MenuV2LikedBuildingDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2MealContextDto(
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val date: LocalDate,
    val type: String,
    val price: Int?,
    val noMeat: Boolean,
    val menuName: String,
) {
    companion object {
        fun from(row: MenuV2MealContextRow): MenuV2MealContextDto =
            MenuV2MealContextDto(
                date = row.getDate(),
                type = row.getType().toMealTypeCode(),
                price = row.getPrice(),
                noMeat = row.getNoMeat(),
                menuName = row.getOriginalName(),
            )
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2DetailsDto(
    val menuId: Long,
    val menuName: String,
    val restaurantId: Int,
    val restaurantName: String,
    val buildingNumber: String,
    val buildingName: String?,
    val score: Double?,
    val reviewCnt: Int,
    val likeCnt: Int,
    val isLiked: Boolean,
    val meals: List<MenuV2MealContextDto>,
) {
    companion object {
        fun from(
            row: MenuV2DetailRow,
            meals: List<MenuV2MealContextDto>,
        ): MenuV2DetailsDto =
            MenuV2DetailsDto(
                menuId = row.getMenuId(),
                menuName = row.getMenuName(),
                restaurantId = row.getRestaurantId(),
                restaurantName = row.getRestaurantName(),
                buildingNumber = row.getBuildingNumber(),
                buildingName = row.getBuildingName(),
                score = row.getScore(),
                reviewCnt = row.getReviewCnt(),
                likeCnt = row.getLikeCnt(),
                isLiked = row.getIsLiked() == 1,
                meals = meals,
            )
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2AlarmDto(
    val menuId: Long,
    val menuName: String,
    val restaurantId: Int,
    val restaurantName: String,
    val buildingNumber: String,
    val buildingName: String?,
    val score: Double?,
    val reviewCnt: Int,
    val likeCnt: Int,
    val isLiked: Boolean,
    val alarm: Boolean,
    val meals: List<MenuV2MealContextDto>,
) {
    companion object {
        fun from(
            menu: MenuV2DetailsDto,
            alarm: Boolean,
        ): MenuV2AlarmDto =
            MenuV2AlarmDto(
                menuId = menu.menuId,
                menuName = menu.menuName,
                restaurantId = menu.restaurantId,
                restaurantName = menu.restaurantName,
                buildingNumber = menu.buildingNumber,
                buildingName = menu.buildingName,
                score = menu.score,
                reviewCnt = menu.reviewCnt,
                likeCnt = menu.likeCnt,
                isLiked = menu.isLiked,
                alarm = alarm,
                meals = menu.meals,
            )
    }
}

fun String.toMealTypeCode(): String =
    when (this) {
        "BREAKFAST" -> "BR"
        "LUNCH" -> "LU"
        "DINNER" -> "DN"
        else -> this
    }
