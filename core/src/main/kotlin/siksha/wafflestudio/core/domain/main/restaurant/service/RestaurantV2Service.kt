package siksha.wafflestudio.core.domain.main.restaurant.service

import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFoundException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantLikeV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2BuildingResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2LikeResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2VisibleResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantLikeV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class RestaurantV2Service(
    private val restaurantRepository: RestaurantV2Repository,
    private val userRepository: UserRepository,
    private val restaurantCustomRepository: RestaurantCustomV2Repository,
    private val buildingCustomRepository: BuildingCustomV2Repository,
    private val restaurantLikeRepository: RestaurantLikeV2Repository,
) {
    @Cacheable(value = ["restaurantCache"])
    fun getAllRestaurants(): RestaurantV2ListResponseDto {
        val restaurants = restaurantRepository.findAllForList()
        return restaurants.toGroupedResponse()
    }

    fun getAllPersonalizedRestaurants(userId: Int): RestaurantV2ListResponseDto {
        val restaurants = restaurantRepository.findAllForList()
        val restaurantCustomMap = restaurantCustomRepository.findAllByUserId(userId).associateBy { it.restaurant.id }
        val buildingCustomMap = buildingCustomRepository.findAllByUserId(userId).associateBy { it.building.id }
        val likedRestaurantIds = restaurantLikeRepository.findAllByUserId(userId).map { it.restaurant.id }.toSet()

        val resultRestaurants =
            restaurants.sortedWith(
                compareBy(
                    { restaurant -> if (buildingCustomMap[restaurant.building.id]?.orderIndex != null) 0 else 1 },
                    { restaurant -> buildingCustomMap[restaurant.building.id]?.orderIndex ?: restaurant.building.defaultOrder },
                    { restaurant -> restaurant.building.defaultOrder },
                    { restaurant -> restaurant.building.id },
                    { restaurant -> if (restaurantCustomMap[restaurant.id]?.orderIndex != null) 0 else 1 },
                    { restaurant -> restaurantCustomMap[restaurant.id]?.orderIndex ?: restaurant.defaultOrder },
                    { restaurant -> restaurant.defaultOrder },
                    { restaurant -> restaurant.id },
                ),
            )

        return resultRestaurants.toGroupedResponse(
            restaurantCustomMap = restaurantCustomMap,
            buildingVisibleMap = buildingCustomMap.mapValues { it.value.visible },
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

    @Transactional
    fun setRestaurantVisible(
        userId: Int,
        restaurantId: Int,
        visible: Boolean,
    ): RestaurantV2VisibleResponseDto {
        val restaurant = restaurantRepository.findById(restaurantId).orElseThrow { RestaurantNotFoundException() }
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val existing = restaurantCustomRepository.findRestaurantCustomV2ByUserIdAndRestaurantId(userId, restaurant.id)
        val custom = existing ?: RestaurantCustomV2(user = user, building = restaurant.building, restaurant = restaurant)

        custom.visible = visible
        if (custom.visible && custom.orderIndex == null) {
            if (existing != null) {
                restaurantCustomRepository.delete(existing)
            }
        } else {
            restaurantCustomRepository.save(custom)
        }

        return RestaurantV2VisibleResponseDto(
            restaurantId = restaurantId,
            visible = visible,
        )
    }

    private fun List<RestaurantV2>.toGroupedResponse(
        restaurantCustomMap: Map<Int, RestaurantCustomV2> = emptyMap(),
        buildingVisibleMap: Map<Int, Boolean> = emptyMap(),
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
                            visible = buildingVisibleMap[building.id] ?: true,
                            restaurants =
                                restaurantsInBuilding.map { restaurant ->
                                    val custom = restaurantCustomMap[restaurant.id]
                                    RestaurantV2ResponseDto.from(
                                        restaurant = restaurant,
                                        liked = restaurant.id in likedRestaurantIds,
                                        visible = custom?.visible ?: true,
                                    )
                                },
                        )
                    },
        )
}
