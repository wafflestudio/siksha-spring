package siksha.wafflestudio.core.domain.main.restaurant.service

import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.InvalidCustomException
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFoundException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2Document
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2Item
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2Json
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantLikeV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.data.itemOf
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2BuildingResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2LikeResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantLikeV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class RestaurantV2Service(
    private val restaurantRepository: RestaurantV2Repository,
    private val userRepository: UserRepository,
    private val buildingRepository: BuildingV2Repository,
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
        val buildings = buildingRepository.findAllForList()
        val buildingCustomMap =
            buildingCustomRepository
                .findByUserId(userId)
                ?.let { requireCompleteBuildingDocument(CustomV2Json.parse(it.customs), buildings) }
        val restaurantCustomMap =
            restaurantCustomRepository
                .findByUserId(userId)
                ?.let { requireCompleteRestaurantDocument(CustomV2Json.parse(it.customs), restaurants) }
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

    private fun requireCompleteBuildingDocument(
        document: CustomV2Document,
        buildings: List<BuildingV2>,
    ): Map<Int, CustomV2Item> {
        val expectedIds = buildings.map { it.id }.toSet()
        val itemMap = requireCompleteItems(document, expectedIds)
        validateDenseOrder(itemMap.values.map { it.order!! })
        return itemMap
    }

    private fun requireCompleteRestaurantDocument(
        document: CustomV2Document,
        restaurants: List<RestaurantV2>,
    ): Map<Int, CustomV2Item> {
        val expectedIds = restaurants.map { it.id }.toSet()
        val itemMap = requireCompleteItems(document, expectedIds)
        restaurants.groupBy { it.building.id }.values.forEach { restaurantsInBuilding ->
            validateDenseOrder(restaurantsInBuilding.map { itemMap[it.id]!!.order!! })
        }
        return itemMap
    }

    private fun requireCompleteItems(
        document: CustomV2Document,
        expectedIds: Set<Int>,
    ): Map<Int, CustomV2Item> {
        val actualIds =
            document.items.keys
                .map { it.toIntOrNull() ?: throw InvalidCustomException() }
                .toSet()
        if (actualIds != expectedIds) {
            throw InvalidCustomException()
        }
        return expectedIds.associateWith { id ->
            document.itemOf(id)?.takeIf { it.isComplete() } ?: throw InvalidCustomException()
        }
    }

    private fun validateDenseOrder(orders: List<Int>) {
        if (orders.toSet() != (1..orders.size).toSet()) {
            throw InvalidCustomException()
        }
    }
}
