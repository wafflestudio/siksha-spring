package siksha.wafflestudio.core.domain.main.restaurant.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import siksha.wafflestudio.core.domain.main.restaurant.data.CornerV2
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
        @JsonProperty("building")
        val building: String?,
        @JsonProperty("buildingNumber")
        val buildingNumber: String,
        @JsonProperty("buildingName")
        val buildingName: String?,
        @JsonProperty("restaurantName")
        val restaurantName: String,
        @JsonProperty("cornerName")
        val cornerName: String?,
        @JsonProperty("addr")
        val addr: String?,
        @JsonProperty("lat")
        val lat: BigDecimal?,
        @JsonProperty("lng")
        val lng: BigDecimal?,
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
                corner: CornerV2,
                liked: Boolean = false,
                visible: Boolean = true,
            ): RestaurantV2ResponseDto {
                val restaurant = corner.restaurant
                val building = restaurant.building
                val displayName = listOfNotNull(restaurant.name, corner.name).joinToString(" - ")

                return RestaurantV2ResponseDto(
                    id = corner.id,
                    code = displayName,
                    nameKr = displayName,
                    nameEn = null,
                    building = building.number,
                    buildingNumber = building.number,
                    buildingName = building.name,
                    restaurantName = restaurant.name,
                    cornerName = corner.name,
                    addr = restaurant.address,
                    liked = liked,
                    visible = visible,
                    lat = restaurant.latitude,
                    lng = restaurant.longitude,
                    operatingHours = EtcUtils.convertEtc(restaurant.operatingHours),
                    ownerId = restaurant.ownerId,
                    createdAt = restaurant.createdAt,
                    updatedAt = restaurant.updatedAt,
                )
            }
        }
    }

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2ListResponseDto(
    val count: Int,
    val result: List<RestaurantV2ResponseDto>,
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
data class RestaurantV2VisibleResponseDto(
    @field:JsonProperty("id")
    val restaurantId: Int,
    val visible: Boolean,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2VisibleRequestDto(
    @field:JsonProperty("visible")
    val visible: Boolean,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2OrderResponseDto(
    @field:JsonProperty("order")
    val restaurantOrder: List<Int>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2OrderUpdateResponseDto(
    @field:JsonProperty("order")
    val order: List<Int>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantV2OrderUpdateRequestDto(
    @field:JsonProperty("order")
    val order: List<Int>,
)
