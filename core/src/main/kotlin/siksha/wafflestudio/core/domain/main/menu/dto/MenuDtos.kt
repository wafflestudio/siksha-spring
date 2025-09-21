package siksha.wafflestudio.core.domain.main.menu.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import siksha.wafflestudio.core.domain.main.restaurant.data.Restaurant
import siksha.wafflestudio.core.util.EtcUtils
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

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
    ) {
        companion object {
            fun from(
                menu: MenuSummary,
                likeInfo: MenuLikeSummary?,
            ): MenuInListDto {
                return MenuInListDto(
                    createdAt = menu.getCreatedAt().toLocalDateTime().atOffset(ZoneOffset.UTC),
                    updatedAt = menu.getUpdatedAt().toLocalDateTime().atOffset(ZoneOffset.UTC),
                    id = menu.getId(),
                    code = menu.getCode(),
                    nameKr = menu.getNameKr(),
                    nameEn = menu.getNameEn(),
                    price = menu.getPrice(),
                    etc = EtcUtils.convertMenuEtc(menu.getEtc()),
                    score = menu.getScore(),
                    reviewCnt = menu.getReviewCnt(),
                    likeCnt = likeInfo?.getLikeCnt() ?: 0,
                    isLiked = likeInfo?.getIsLiked() == true,
                )
            }
        }
    }

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
    ) {
        companion object {
            fun from(
                restaurant: Restaurant,
                menus: List<MenuInListDto>,
            ): RestaurantInListDto {
                return RestaurantInListDto(
                    createdAt = restaurant.createdAt,
                    updatedAt = restaurant.updatedAt,
                    id = restaurant.id,
                    code = restaurant.code,
                    nameKr = restaurant.nameKr,
                    nameEn = restaurant.nameEn,
                    addr = restaurant.addr,
                    lat = restaurant.lat,
                    lng = restaurant.lng,
                    etc = EtcUtils.convertRestEtc(restaurant.etc),
                    menus = menus,
                )
            }
        }
    }

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
    ) {
        companion object {
            fun from(
                menu: MenuSummary,
                likeInfo: MenuLikeSummary?,
            ): MenuDetailsDto {
                return MenuDetailsDto(
                    createdAt = menu.getCreatedAt().toLocalDateTime().atOffset(ZoneOffset.UTC),
                    updatedAt = menu.getUpdatedAt().toLocalDateTime().atOffset(ZoneOffset.UTC),
                    id = menu.getId(),
                    restaurantId = menu.getRestaurantId(),
                    code = menu.getCode(),
                    date = menu.getDate(),
                    type = menu.getType(),
                    nameKr = menu.getNameKr(),
                    nameEn = menu.getNameEn(),
                    price = menu.getPrice(),
                    etc = EtcUtils.convertMenuEtc(menu.getEtc()),
                    score = menu.getScore(),
                    reviewCnt = menu.getReviewCnt(),
                    isLiked = likeInfo?.getIsLiked() == true,
                    likeCnt = likeInfo?.getLikeCnt() ?: 0,
                )
            }
        }
    }
