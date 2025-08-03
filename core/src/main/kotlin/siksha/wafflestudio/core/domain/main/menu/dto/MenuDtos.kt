package siksha.wafflestudio.core.domain.main.menu.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDate
import java.time.OffsetDateTime

// /menus 요청에 대한 Menu 단위 Dto
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuInListDto
    @JsonCreator
    constructor(
        @JsonProperty("created_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        val createdAt: OffsetDateTime,
        @JsonProperty("updated_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        val updatedAt: OffsetDateTime,
        @JsonProperty("id")
        val id: Int,
        @JsonProperty("code")
        val code: String,
        @JsonProperty("name_kr")
        val nameKr: String?,
        @JsonProperty("name_en")
        val nameEn: String?,
        @JsonProperty("price")
        val price: Int?,
        @JsonProperty("etc")
        val etc: List<String>,
        @JsonProperty("score")
        val score: Double?,
        @JsonProperty("review_cnt")
        val reviewCnt: Int,
        @JsonProperty("like_cnt")
        val likeCnt: Int,
        @JsonProperty("is_liked")
        val isLiked: Boolean,
    )

// /menus 요청에 대한 (Menu+) Restaurant 단위 Dto
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantInListDto
    @JsonCreator
    constructor(
        @JsonProperty("created_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        val createdAt: OffsetDateTime,
        @JsonProperty("updated_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        val updatedAt: OffsetDateTime,
        @JsonProperty("id")
        val id: Int,
        @JsonProperty("code")
        val code: String,
        @JsonProperty("name_kr")
        val nameKr: String?,
        @JsonProperty("name_en")
        val nameEn: String?,
        @JsonProperty("addr")
        val addr: String?,
        @JsonProperty("lat")
        val lat: Double?,
        @JsonProperty("lng")
        val lng: Double?,
        @JsonProperty("etc")
        val etc: JsonNode,
        @JsonProperty("menus")
        val menus: List<MenuInListDto>,
    )

// /menus 요청에 대한 result Dto
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DateWithTypeInListDto
    @JsonCreator
    constructor(
        @JsonProperty("date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        val date: LocalDate,
        @JsonProperty("date_type")
        val dateType: String,
        @JsonProperty("BR")
        val BR: List<RestaurantInListDto> = emptyList(),
        @JsonProperty("LU")
        val LU: List<RestaurantInListDto> = emptyList(),
        @JsonProperty("DN")
        val DN: List<RestaurantInListDto> = emptyList(),
    )

// /menus 요청에 대한 전체 Dto
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuListResponseDto
    @JsonCreator
    constructor(
        @JsonProperty("count")
        val count: Int,
        @JsonProperty("result")
        val result: List<DateWithTypeInListDto>,
    )

// menus/{menu_id} 요청에 대한 Dto
// menus/{menu_id)/like, unlike 요청에 대한 Dto
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MenuDetailsDto
    @JsonCreator
    constructor(
        @JsonProperty("created_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        val createdAt: OffsetDateTime,
        @JsonProperty("updated_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        val updatedAt: OffsetDateTime,
        @JsonProperty("id")
        val id: Int,
        @JsonProperty("restaurant_id")
        val restaurantId: Int,
        @JsonProperty("code")
        val code: String,
        @JsonProperty("date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        val date: LocalDate,
        @JsonProperty("type")
        val type: String,
        @JsonProperty("name_kr")
        val nameKr: String?,
        @JsonProperty("name_en")
        val nameEn: String?,
        @JsonProperty("price")
        val price: Int?,
        @JsonProperty("etc")
        val etc: List<String>,
        @JsonProperty("score")
        val score: Double?,
        @JsonProperty("review_cnt")
        val reviewCnt: Int,
        @JsonProperty("is_liked")
        val isLiked: Boolean,
        @JsonProperty("like_cnt")
        val likeCnt: Int,
    )
