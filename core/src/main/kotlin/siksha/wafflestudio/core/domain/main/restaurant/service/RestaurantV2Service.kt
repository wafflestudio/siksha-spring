package siksha.wafflestudio.core.domain.main.restaurant.service

import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFoundException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2BuildingResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2LikeResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2VisibleResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class RestaurantV2Service(
    private val restaurantRepository: RestaurantV2Repository,
    private val userRepository: UserRepository,
    private val restaurantCustomRepository: RestaurantCustomV2Repository,
    private val buildingCustomRepository: BuildingCustomV2Repository,
) {
    @Cacheable(value = ["restaurantCache"])
    fun getAllRestaurants(): RestaurantV2ListResponseDto {
        val restaurants = restaurantRepository.findAllForList()
        return restaurants.toGroupedResponse()
    }

    fun getAllPersonalizedRestaurants(userId: Int): RestaurantV2ListResponseDto {
        val restaurants = restaurantRepository.findAllForList()
        val restaurantCustoms = restaurantCustomRepository.findAllByUserId(userId)
        val restaurantCustomMap = restaurantCustoms.associateBy { it.restaurant.id }
        val buildingCustoms = buildingCustomRepository.findAllByUserId(userId)
        val buildingCustomMap = buildingCustoms.associateBy { it.building.id }

        val resultRestaurants =
            restaurants.sortedWith(
                compareBy(
                    { restaurant -> if (buildingCustomMap[restaurant.building.id]?.orderIndex != null) 0 else 1 },
                    { restaurant -> buildingCustomMap[restaurant.building.id]?.orderIndex ?: restaurant.building.id },
                    { restaurant -> restaurant.building.id },
                    { restaurant -> restaurant.displayOrder },
                    { restaurant -> restaurant.id },
                ),
            )

        return resultRestaurants.toGroupedResponse(restaurantCustomMap)
    }

    @Transactional
    fun setRestaurantLike(
        userId: Int,
        restaurantId: Int,
        like: Boolean,
    ): RestaurantV2LikeResponseDto {
        val restaurant =
            restaurantRepository
                .findById(restaurantId)
                .orElseThrow { RestaurantNotFoundException() }
        val user =
            userRepository
                .findById(userId)
                .orElseThrow { UserNotFoundException() }

        val custom =
            restaurantCustomRepository.findRestaurantCustomV2ByUserIdAndRestaurantId(userId, restaurant.id)
                ?: RestaurantCustomV2(user = user, restaurant = restaurant)

        custom.like = like
        val savedCustom = restaurantCustomRepository.save(custom)

        return RestaurantV2LikeResponseDto(
            restaurantId = restaurantId,
            liked = savedCustom.like,
        )
    }

    @Transactional
    fun setRestaurantVisible(
        userId: Int,
        restaurantId: Int,
        visible: Boolean,
    ): RestaurantV2VisibleResponseDto {
        val restaurant =
            restaurantRepository
                .findById(restaurantId)
                .orElseThrow { RestaurantNotFoundException() }
        val user =
            userRepository
                .findById(userId)
                .orElseThrow { UserNotFoundException() }

        val custom =
            restaurantCustomRepository.findRestaurantCustomV2ByUserIdAndRestaurantId(userId, restaurant.id)
                ?: RestaurantCustomV2(user = user, restaurant = restaurant)

        custom.visible = visible
        val savedCustom = restaurantCustomRepository.save(custom)

        return RestaurantV2VisibleResponseDto(
            restaurantId = restaurantId,
            visible = savedCustom.visible,
        )
    }

    private fun List<RestaurantV2>.toGroupedResponse(
        restaurantCustomMap: Map<Int, RestaurantCustomV2> = emptyMap(),
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
                            restaurants =
                                restaurantsInBuilding.map { restaurant ->
                                    val custom = restaurantCustomMap[restaurant.id]
                                    RestaurantV2ResponseDto.from(
                                        restaurant = restaurant,
                                        liked = custom?.like ?: false,
                                        visible = custom?.visible ?: true,
                                    )
                                },
                        )
                    },
        )
}
