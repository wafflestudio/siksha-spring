package siksha.wafflestudio.core.domain.main.restaurant.service

import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFoundException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2Item
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantLikeV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2BuildingResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2LikeResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantLikeV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class RestaurantV2Service(
    private val restaurantRepository: RestaurantV2Repository,
    private val userRepository: UserRepository,
    private val customService: CustomV2Service,
    private val restaurantLikeRepository: RestaurantLikeV2Repository,
) {
    @Cacheable(value = ["restaurantCache"])
    fun getAllRestaurants(): RestaurantV2ListResponseDto {
        val restaurants = restaurantRepository.findAllForList()
        return restaurants.toGroupedResponse()
    }

    fun getAllPersonalizedRestaurants(userId: Int): RestaurantV2ListResponseDto {
        val restaurants = restaurantRepository.findAllForList()
        val customMaps = customService.getCustomMaps(userId)
        val buildingCustomMap = customMaps?.buildingCustomMap
        val restaurantCustomMap = customMaps?.restaurantCustomMap
        val likedRestaurantIds = restaurantLikeRepository.findAllByUserId(userId).map { it.restaurant.id }.toSet()

        val resultRestaurants =
            restaurants.sortedWith(
                compareBy(
                    { restaurant -> buildingCustomMap?.get(restaurant.building.id)?.order ?: restaurant.building.defaultOrder },
                    { restaurant -> restaurant.building.id },
                    { restaurant -> restaurantCustomMap?.get(restaurant.id)?.order ?: restaurant.defaultOrder },
                    { restaurant -> restaurant.id },
                ),
            )

        return resultRestaurants.toGroupedResponse(
            restaurantCustomMap = restaurantCustomMap,
            buildingCustomMap = buildingCustomMap,
            likedRestaurantIds = likedRestaurantIds,
        )
    }

    @Transactional
    fun setRestaurantLike(
        userId: Int,
        restaurantId: Int,
        like: Boolean,
    ): RestaurantV2LikeResponseDto {
        val restaurant = restaurantRepository.findById(restaurantId).orElseThrow { RestaurantNotFoundException() }
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val existing = restaurantLikeRepository.findRestaurantLikeV2ByUserIdAndRestaurantId(userId, restaurantId)

        if (like && existing == null) {
            restaurantLikeRepository.save(RestaurantLikeV2(user = user, restaurant = restaurant))
        }
        if (!like && existing != null) {
            restaurantLikeRepository.delete(existing)
        }

        return RestaurantV2LikeResponseDto(
            restaurantId = restaurantId,
            liked = like,
        )
    }

    private fun List<RestaurantV2>.toGroupedResponse(
        restaurantCustomMap: Map<Int, CustomV2Item>? = null,
        buildingCustomMap: Map<Int, CustomV2Item>? = null,
        likedRestaurantIds: Set<Int> = emptySet(),
    ): RestaurantV2ListResponseDto =
        RestaurantV2ListResponseDto(
            count = size,
            result =
                groupBy { it.building.id }
                    .values
                    .map { restaurantsInBuilding ->
                        val building = restaurantsInBuilding.first().building
                        RestaurantV2BuildingResponseDto.from(
                            building = building,
                            visible = buildingCustomMap?.get(building.id)?.visible ?: true,
                            restaurants =
                                restaurantsInBuilding.map { restaurant ->
                                    RestaurantV2ResponseDto.from(
                                        restaurant = restaurant,
                                        liked = restaurant.id in likedRestaurantIds,
                                        visible = restaurantCustomMap?.get(restaurant.id)?.visible ?: true,
                                    )
                                },
                        )
                    },
        )
}
