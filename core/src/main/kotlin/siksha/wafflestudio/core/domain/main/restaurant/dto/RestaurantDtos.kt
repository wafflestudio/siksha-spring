package siksha.wafflestudio.core.domain.main.restaurant.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import siksha.wafflestudio.core.domain.main.restaurant.data.Restaurant
import java.time.OffsetDateTime
import siksha.wafflestudio.core.util.EtcUtils

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantResponseDto @JsonCreator constructor (
    @JsonProperty("id")
    val id: Int = 0,
    @JsonProperty("code")
    val code: String,
    @JsonProperty("nameKr")
    val nameKr: String?,
    @JsonProperty("nameEn")
    val nameEn: String?,
    @JsonProperty("addr")
    val addr: String?,
    @JsonProperty("lat")
    val lat: Double?,
    @JsonProperty("lng")
    val lng: Double?,
    @JsonProperty("etc")
    val etc: JsonNode,
    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
    val createdAt: OffsetDateTime,
    @JsonProperty("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun from(restaurant: Restaurant): RestaurantResponseDto {
            return RestaurantResponseDto(
                id = restaurant.id,
                code = restaurant.code,
                nameKr = restaurant.nameKr,
                nameEn = restaurant.nameEn,
                addr = restaurant.addr,
                lat = restaurant.lat,
                lng = restaurant.lng,
                etc = EtcUtils.convertRestEtc(restaurant.etc),
                createdAt = restaurant.createdAt,
                updatedAt = restaurant.updatedAt,
            )
        }
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RestaurantListResponseDto(
    val count: Int,
    val result: List<RestaurantResponseDto>,
)
