package siksha.wafflestudio.core.domain.main.menu.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface MenuV2MealRow {
    fun getMealMenuId(): Long

    fun getMealId(): Long

    fun getMenuId(): Long

    fun getMenuName(): String

    fun getOriginalName(): String

    fun getRestaurantId(): Int

    fun getDate(): LocalDate

    fun getType(): String

    fun getPrice(): Int?

    fun getNoMeat(): Boolean

    fun getMenuCreatedAt(): Timestamp

    fun getScore(): Double?

    fun getReviewCnt(): Int

    fun getLikeCnt(): Int

    fun getIsLiked(): Int
}

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

    fun getMenuCreatedAt(): Timestamp

    fun getScore(): Double?

    fun getReviewCnt(): Int

    fun getLikeCnt(): Int

    fun getIsLiked(): Int
}

interface MenuV2MealContextRow {
    fun getMealMenuId(): Long

    fun getMealId(): Long

    fun getDate(): LocalDate

    fun getType(): String

    fun getPrice(): Int?

    fun getNoMeat(): Boolean

    fun getOriginalName(): String

    fun getMealCreatedAt(): Timestamp
}

interface MenuV2LikedMenuRow {
    fun getMenuId(): Long

    fun getMenuName(): String

    fun getRestaurantId(): Int

    fun getMenuCreatedAt(): Timestamp

    fun getScore(): Double?

    fun getReviewCnt(): Int

    fun getLikeCnt(): Int

    fun getAlarm(): Int
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2InListDto(
    val id: Long,
    val mealId: Long,
    val mealMenuId: Long,
    val code: String,
    val nameKr: String,
    val nameEn: String? = null,
    val originalName: String,
    val price: Int?,
    val noMeat: Boolean,
    val score: Double?,
    val reviewCnt: Int,
    val likeCnt: Int,
    val isLiked: Boolean,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun from(row: MenuV2MealRow): MenuV2InListDto =
            MenuV2InListDto(
                id = row.getMenuId(),
                mealId = row.getMealId(),
                mealMenuId = row.getMealMenuId(),
                code = row.getMenuName(),
                nameKr = row.getMenuName(),
                originalName = row.getOriginalName(),
                price = row.getPrice(),
                noMeat = row.getNoMeat(),
                score = row.getScore(),
                reviewCnt = row.getReviewCnt(),
                likeCnt = row.getLikeCnt(),
                isLiked = row.getIsLiked() == 1,
                createdAt = row.getMenuCreatedAt().toInstant().atOffset(ZoneOffset.UTC),
            )
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2RestaurantInListDto(
    val id: Int,
    val code: String,
    val nameKr: String,
    val nameEn: String? = null,
    val restaurantName: String,
    val visible: Boolean,
    val menus: List<MenuV2InListDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2BuildingInListDto(
    val buildingNumber: String,
    val buildingName: String?,
    val addr: String?,
    val lat: BigDecimal?,
    val lng: BigDecimal?,
    val visible: Boolean,
    val restaurants: List<MenuV2RestaurantInListDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2DateWithTypeDto(
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val date: LocalDate,
    val dateType: String,
    @field:JsonProperty("BR")
    val breakfast: List<MenuV2BuildingInListDto> = emptyList(),
    @field:JsonProperty("LU")
    val lunch: List<MenuV2BuildingInListDto> = emptyList(),
    @field:JsonProperty("DN")
    val dinner: List<MenuV2BuildingInListDto> = emptyList(),
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2ListResponseDto(
    val count: Int,
    val result: List<MenuV2DateWithTypeDto>,
)

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
    val id: Long,
    val code: String,
    val nameKr: String,
    val nameEn: String? = null,
    val score: Double?,
    val reviewCnt: Int,
    val likeCnt: Int,
    val isLiked: Boolean = true,
    val alarm: Boolean,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun from(row: MenuV2LikedMenuRow): MenuV2LikedMenuDto =
            MenuV2LikedMenuDto(
                id = row.getMenuId(),
                code = row.getMenuName(),
                nameKr = row.getMenuName(),
                score = row.getScore(),
                reviewCnt = row.getReviewCnt(),
                likeCnt = row.getLikeCnt(),
                alarm = row.getAlarm() == 1,
                createdAt = row.getMenuCreatedAt().toInstant().atOffset(ZoneOffset.UTC),
            )
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2LikedRestaurantDto(
    val id: Int,
    val code: String,
    val nameKr: String,
    val nameEn: String? = null,
    val restaurantName: String,
    val visible: Boolean,
    val menus: List<MenuV2LikedMenuDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2LikedBuildingDto(
    val buildingNumber: String,
    val buildingName: String?,
    val addr: String?,
    val lat: BigDecimal?,
    val lng: BigDecimal?,
    val visible: Boolean,
    val restaurants: List<MenuV2LikedRestaurantDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2LikedListResponseDto(
    val count: Int,
    val result: List<MenuV2LikedBuildingDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2MealContextDto(
    val mealMenuId: Long,
    val mealId: Long,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val date: LocalDate,
    val type: String,
    val price: Int?,
    val noMeat: Boolean,
    val originalName: String,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun from(row: MenuV2MealContextRow): MenuV2MealContextDto =
            MenuV2MealContextDto(
                mealMenuId = row.getMealMenuId(),
                mealId = row.getMealId(),
                date = row.getDate(),
                type = row.getType().toMealTypeCode(),
                price = row.getPrice(),
                noMeat = row.getNoMeat(),
                originalName = row.getOriginalName(),
                createdAt = row.getMealCreatedAt().toInstant().atOffset(ZoneOffset.UTC),
            )
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2DetailsDto(
    val id: Long,
    val code: String,
    val nameKr: String,
    val nameEn: String? = null,
    val restaurantId: Int,
    val restaurantName: String,
    val buildingNumber: String,
    val buildingName: String?,
    val score: Double?,
    val reviewCnt: Int,
    val likeCnt: Int,
    val isLiked: Boolean,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    val createdAt: OffsetDateTime,
    val meals: List<MenuV2MealContextDto>,
) {
    companion object {
        fun from(
            row: MenuV2DetailRow,
            meals: List<MenuV2MealContextDto>,
        ): MenuV2DetailsDto =
            MenuV2DetailsDto(
                id = row.getMenuId(),
                code = row.getMenuName(),
                nameKr = row.getMenuName(),
                restaurantId = row.getRestaurantId(),
                restaurantName = row.getRestaurantName(),
                buildingNumber = row.getBuildingNumber(),
                buildingName = row.getBuildingName(),
                score = row.getScore(),
                reviewCnt = row.getReviewCnt(),
                likeCnt = row.getLikeCnt(),
                isLiked = row.getIsLiked() == 1,
                createdAt = row.getMenuCreatedAt().toInstant().atOffset(ZoneOffset.UTC),
                meals = meals,
            )
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuV2AlarmDto(
    val id: Long,
    val code: String,
    val nameKr: String,
    val nameEn: String? = null,
    val restaurantId: Int,
    val restaurantName: String,
    val buildingNumber: String,
    val buildingName: String?,
    val score: Double?,
    val reviewCnt: Int,
    val likeCnt: Int,
    val isLiked: Boolean,
    val alarm: Boolean,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    val createdAt: OffsetDateTime,
    val meals: List<MenuV2MealContextDto>,
) {
    companion object {
        fun from(
            menu: MenuV2DetailsDto,
            alarm: Boolean,
        ): MenuV2AlarmDto =
            MenuV2AlarmDto(
                id = menu.id,
                code = menu.code,
                nameKr = menu.nameKr,
                nameEn = menu.nameEn,
                restaurantId = menu.restaurantId,
                restaurantName = menu.restaurantName,
                buildingNumber = menu.buildingNumber,
                buildingName = menu.buildingName,
                score = menu.score,
                reviewCnt = menu.reviewCnt,
                likeCnt = menu.likeCnt,
                isLiked = menu.isLiked,
                alarm = alarm,
                createdAt = menu.createdAt,
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
