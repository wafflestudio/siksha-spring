package siksha.wafflestudio.core.domain.main.restaurant.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.BuildingNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidCustomException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2Document
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2Item
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2Json
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.data.itemOf
import siksha.wafflestudio.core.domain.main.restaurant.data.setCustom
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2CustomItemDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2CustomResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2CustomUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2CustomUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2CustomItemDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2CustomResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2CustomUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2CustomUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class BuildingCustomV2Service(
    private val userRepository: UserRepository,
    private val buildingRepository: BuildingV2Repository,
    private val restaurantRepository: RestaurantV2Repository,
    private val buildingCustomRepository: BuildingCustomV2Repository,
    private val restaurantCustomRepository: RestaurantCustomV2Repository,
) {
    fun getBuildingCustoms(userId: Int): BuildingV2CustomResponseDto {
        val buildings = buildingRepository.findAllForList()
        val custom = buildingCustomRepository.findByUserId(userId)
        val itemMap = custom?.let { requireCompleteBuildingDocument(CustomV2Json.parse(it.customs), buildings) }

        return BuildingV2CustomResponseDto(
            customs = buildings.toBuildingCustomDtos(itemMap),
        )
    }

    @Transactional
    fun updateBuildingCustoms(
        userId: Int,
        request: BuildingV2CustomUpdateRequestDto,
    ): BuildingV2CustomUpdateResponseDto {
        userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val buildings = buildingRepository.findAllForList()
        val buildingMap = buildings.associateBy { it.number }
        val requestMap = request.customs.associateBy { it.buildingNumber }

        if (requestMap.size != request.customs.size || requestMap.keys != buildingMap.keys) {
            throw InvalidCustomException()
        }
        validateDenseOrder(request.customs.map { it.order })

        val document = CustomV2Document()
        request.customs.forEach { custom ->
            val building = buildingMap[custom.buildingNumber] ?: throw InvalidCustomException()
            document.setCustom(building.id, custom.order, custom.visible)
        }

        buildingCustomRepository.save(
            BuildingCustomV2(
                userId = userId,
                customs = CustomV2Json.stringify(document),
            ),
        )

        val itemMap = requireCompleteBuildingDocument(document, buildings)
        return BuildingV2CustomUpdateResponseDto(
            customs = buildings.toBuildingCustomDtos(itemMap),
        )
    }

    fun getRestaurantCustoms(
        userId: Int,
        buildingNumber: String,
    ): RestaurantV2CustomResponseDto {
        val restaurants = findRestaurantsInBuildingOrThrow(buildingNumber)
        val custom = restaurantCustomRepository.findByUserId(userId)
        val itemMap = custom?.let { requireCompleteRestaurantDocument(CustomV2Json.parse(it.customs), restaurants) }

        return RestaurantV2CustomResponseDto(
            customs = restaurants.toRestaurantCustomDtos(itemMap),
        )
    }

    @Transactional
    fun updateRestaurantCustoms(
        userId: Int,
        buildingNumber: String,
        request: RestaurantV2CustomUpdateRequestDto,
    ): RestaurantV2CustomUpdateResponseDto {
        userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val restaurantsInBuilding = findRestaurantsInBuildingOrThrow(buildingNumber)
        val restaurantMap = restaurantsInBuilding.associateBy { it.id }
        val requestMap = request.customs.associateBy { it.restaurantId }

        if (requestMap.size != request.customs.size || requestMap.keys != restaurantMap.keys) {
            throw InvalidCustomException()
        }
        validateDenseOrder(request.customs.map { it.order })

        val allRestaurants = restaurantRepository.findAllForList()
        val existingDocument = CustomV2Json.parse(restaurantCustomRepository.findByUserId(userId)?.customs)
        val document = CustomV2Document()

        allRestaurants.forEach { restaurant ->
            val requested = requestMap[restaurant.id]
            when {
                requested != null -> document.setCustom(restaurant.id, requested.order, requested.visible)
                existingDocument.itemOf(restaurant.id)?.isComplete() == true -> {
                    val existingItem = existingDocument.itemOf(restaurant.id)!!
                    document.setCustom(restaurant.id, existingItem.order!!, existingItem.visible!!)
                }
                else -> document.setCustom(restaurant.id, restaurant.defaultOrder, true)
            }
        }
        requireCompleteRestaurantDocument(document, allRestaurants, exact = true)

        restaurantCustomRepository.save(
            RestaurantCustomV2(
                userId = userId,
                customs = CustomV2Json.stringify(document),
            ),
        )

        val itemMap = requireCompleteRestaurantDocument(document, restaurantsInBuilding)
        return RestaurantV2CustomUpdateResponseDto(
            customs = restaurantsInBuilding.toRestaurantCustomDtos(itemMap),
        )
    }

    private fun findRestaurantsInBuildingOrThrow(buildingNumber: String): List<RestaurantV2> {
        val restaurants = restaurantRepository.findAllByBuildingNumber(buildingNumber)
        if (restaurants.isEmpty() && buildingRepository.findByNumber(buildingNumber) == null) {
            throw BuildingNotFoundException()
        }
        return restaurants
    }

    private fun List<BuildingV2>.toBuildingCustomDtos(itemMap: Map<Int, CustomV2Item>?): List<BuildingV2CustomItemDto> =
        map { building ->
            val item = itemMap?.get(building.id)
            BuildingV2CustomItemDto(
                buildingNumber = building.number,
                order = item?.order ?: building.defaultOrder,
                visible = item?.visible ?: true,
            )
        }.sortedBy { it.order }

    private fun List<RestaurantV2>.toRestaurantCustomDtos(itemMap: Map<Int, CustomV2Item>?): List<RestaurantV2CustomItemDto> =
        map { restaurant ->
            val item = itemMap?.get(restaurant.id)
            RestaurantV2CustomItemDto(
                restaurantId = restaurant.id,
                order = item?.order ?: restaurant.defaultOrder,
                visible = item?.visible ?: true,
            )
        }.sortedBy { it.order }

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
        exact: Boolean = false,
    ): Map<Int, CustomV2Item> {
        val expectedIds = restaurants.map { it.id }.toSet()
        val itemMap = requireCompleteItems(document, expectedIds, exact)
        restaurants.groupBy { it.building.id }.values.forEach { restaurantsInBuilding ->
            validateDenseOrder(restaurantsInBuilding.map { itemMap[it.id]!!.order!! })
        }
        return itemMap
    }

    private fun requireCompleteItems(
        document: CustomV2Document,
        expectedIds: Set<Int>,
        exact: Boolean = true,
    ): Map<Int, CustomV2Item> {
        val actualIds =
            document.items.keys
                .map { it.toIntOrNull() ?: throw InvalidCustomException() }
                .toSet()
        if ((exact && actualIds != expectedIds) || (!exact && !actualIds.containsAll(expectedIds))) {
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
