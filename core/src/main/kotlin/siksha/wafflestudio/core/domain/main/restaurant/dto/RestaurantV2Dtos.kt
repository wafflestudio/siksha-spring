package siksha.wafflestudio.core.domain.main.restaurant.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.util.EtcUtils
import java.math.BigDecimal
import java.time.OffsetDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2ResponseDto
    @JsonCreator
    constructor(
        @JsonProperty("id")
        val id: Int = 0,
        @JsonProperty("code")
        val code: String?,
        @JsonProperty("nameKr")
        val nameKr: String?,
        @JsonProperty("nameEn")
        val nameEn: String?,
        @JsonProperty("restaurantName")
        val restaurantName: String,
        @JsonProperty("liked")
        val liked: Boolean?,
        @JsonProperty("visible")
        val visible: Boolean?,
        @JsonProperty("operatingHours")
        val operatingHours: JsonNode,
        @JsonProperty("ownerId")
        val ownerId: Int?,
        @JsonProperty("created_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
        val createdAt: OffsetDateTime,
        @JsonProperty("updated_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
        val updatedAt: OffsetDateTime,
    ) {
        companion object {
            fun from(
                restaurant: RestaurantV2,
                liked: Boolean = false,
                visible: Boolean = true,
            ): RestaurantV2ResponseDto =
                RestaurantV2ResponseDto(
                    id = restaurant.id,
                    code = restaurant.name,
                    nameKr = restaurant.name,
                    nameEn = null,
                    restaurantName = restaurant.name,
                    liked = liked,
                    visible = visible,
                    operatingHours = EtcUtils.convertEtc(restaurant.operatingHours),
                    ownerId = restaurant.ownerId,
                    createdAt = restaurant.createdAt,
                    updatedAt = restaurant.updatedAt,
                )
        }
    }

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2BuildingResponseDto(
    @JsonProperty("buildingNumber")
    val buildingNumber: String,
    @JsonProperty("buildingName")
    val buildingName: String?,
    @JsonProperty("addr")
    val addr: String?,
    @JsonProperty("lat")
    val lat: BigDecimal?,
    @JsonProperty("lng")
    val lng: BigDecimal?,
    @JsonProperty("visible")
    val visible: Boolean,
    val restaurants: List<RestaurantV2ResponseDto>,
) {
    companion object {
        fun from(
            building: BuildingV2,
            visible: Boolean = true,
            restaurants: List<RestaurantV2ResponseDto>,
        ): RestaurantV2BuildingResponseDto =
            RestaurantV2BuildingResponseDto(
                buildingNumber = building.number,
                buildingName = building.name,
                addr = building.address,
                lat = building.latitude,
                lng = building.longitude,
                visible = visible,
                restaurants = restaurants,
            )
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2ListResponseDto(
    val count: Int,
    val result: List<RestaurantV2BuildingResponseDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2LikeResponseDto(
    @field:JsonProperty("id")
    val restaurantId: Int,
    val liked: Boolean,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2LikeRequestDto(
    @field:JsonProperty("like")
    val like: Boolean,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BuildingV2CustomItemDto(
    val buildingNumber: String,
    val order: Int,
    val visible: Boolean,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BuildingV2CustomResponseDto(
    val customs: List<BuildingV2CustomItemDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BuildingV2CustomUpdateRequestDto(
    val customs: List<BuildingV2CustomItemDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BuildingV2CustomUpdateResponseDto(
    val customs: List<BuildingV2CustomItemDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2CustomItemDto(
    @field:JsonProperty("id")
    val restaurantId: Int,
    val order: Int,
    val visible: Boolean,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2CustomResponseDto(
    val customs: List<RestaurantV2CustomItemDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2CustomUpdateRequestDto(
    val customs: List<RestaurantV2CustomItemDto>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2CustomUpdateResponseDto(
    val customs: List<RestaurantV2CustomItemDto>,
)
