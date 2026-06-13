package siksha.wafflestudio.core.domain.main.restaurant.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.BuildingNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidRestaurantOrderException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2VisibleResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2VisibleRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateResponseDto
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
    fun getBuildingOrder(userId: Int): BuildingV2OrderResponseDto {
        val buildings = buildingRepository.findAllForList()
        val customMap = buildingCustomRepository.findAllByUserId(userId).associateBy { it.building.id }
        val orderedBuildings =
            buildings.sortedWith(
                compareBy(
                    { building -> if (customMap[building.id]?.orderIndex != null) 0 else 1 },
                    { building -> customMap[building.id]?.orderIndex ?: building.defaultOrder },
                    { building -> building.defaultOrder },
                    { building -> building.id },
                ),
            )

        return BuildingV2OrderResponseDto(
            buildingOrder = orderedBuildings.map { it.number },
        )
    }

    @Transactional
    fun changeBuildingOrder(
        userId: Int,
        request: BuildingV2OrderUpdateRequestDto,
    ): BuildingV2OrderUpdateResponseDto {
        val requestedOrderNumbers = request.order
        validateDistinctOrder(requestedOrderNumbers)
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val requestedOrderNumberSet = requestedOrderNumbers.toSet()
        val buildingMap = buildingRepository.findAllByNumberIn(requestedOrderNumberSet).associateBy { it.number }

        if (buildingMap.size != requestedOrderNumberSet.size) {
            throw BuildingNotFoundException()
        }

        val existingCustoms = buildingCustomRepository.findAllByUserId(userId)
        val customMap = existingCustoms.associateBy { it.building.number }
        val customsToSave = mutableListOf<BuildingCustomV2>()
        val customsToDelete = mutableListOf<BuildingCustomV2>()

        existingCustoms
            .filter { it.orderIndex != null && it.building.number !in requestedOrderNumberSet }
            .forEach { custom ->
                custom.orderIndex = null
                if (custom.visible) {
                    customsToDelete.add(custom)
                } else {
                    customsToSave.add(custom)
                }
            }

        requestedOrderNumbers.forEachIndexed { index, buildingNumber ->
            val custom = customMap[buildingNumber] ?: BuildingCustomV2(user = user, building = buildingMap[buildingNumber]!!)
            custom.orderIndex = index + 1
            customsToSave.add(custom)
        }

        if (customsToDelete.isNotEmpty()) {
            buildingCustomRepository.deleteAll(customsToDelete)
        }
        if (customsToSave.isNotEmpty()) {
            buildingCustomRepository.saveAll(customsToSave)
        }

        return BuildingV2OrderUpdateResponseDto(requestedOrderNumbers)
    }

    @Transactional
    fun setBuildingVisible(
        userId: Int,
        buildingNumber: String,
        request: BuildingV2VisibleRequestDto,
    ): BuildingV2VisibleResponseDto {
        val building = buildingRepository.findByNumber(buildingNumber) ?: throw BuildingNotFoundException()
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val existing = buildingCustomRepository.findByUserIdAndBuildingNumber(userId, buildingNumber)
        val custom = existing ?: BuildingCustomV2(user = user, building = building)

        custom.visible = request.visible
        if (custom.visible && custom.orderIndex == null) {
            if (existing != null) {
                buildingCustomRepository.delete(existing)
            }
        } else {
            buildingCustomRepository.save(custom)
        }

        return BuildingV2VisibleResponseDto(
            buildingNumber = buildingNumber,
            visible = request.visible,
        )
    }

    fun getRestaurantOrder(
        userId: Int,
        buildingNumber: String,
    ): RestaurantV2OrderResponseDto {
        val restaurants = restaurantRepository.findAllByBuildingNumber(buildingNumber)
        if (restaurants.isEmpty() && buildingRepository.findByNumber(buildingNumber) == null) {
            throw BuildingNotFoundException()
        }
        val customMap = restaurantCustomRepository.findAllByUserId(userId).associateBy { it.restaurant.id }
        val orderedRestaurants =
            restaurants.sortedWith(
                compareBy(
                    { restaurant -> if (customMap[restaurant.id]?.orderIndex != null) 0 else 1 },
                    { restaurant -> customMap[restaurant.id]?.orderIndex ?: restaurant.defaultOrder },
                    { restaurant -> restaurant.defaultOrder },
                    { restaurant -> restaurant.id },
                ),
            )

        return RestaurantV2OrderResponseDto(
            restaurantOrder = orderedRestaurants.map { it.id },
        )
    }

    @Transactional
    fun changeRestaurantOrder(
        userId: Int,
        buildingNumber: String,
        request: RestaurantV2OrderUpdateRequestDto,
    ): RestaurantV2OrderUpdateResponseDto {
        val requestedOrderIds = request.order
        validateDistinctOrder(requestedOrderIds)
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val building = buildingRepository.findByNumber(buildingNumber) ?: throw BuildingNotFoundException()
        val restaurantsInBuilding = restaurantRepository.findAllByBuildingNumber(buildingNumber)
        val restaurantMap = restaurantsInBuilding.associateBy { it.id }

        if (!restaurantMap.keys.containsAll(requestedOrderIds)) {
            throw InvalidRestaurantOrderException()
        }

        val existingCustoms = restaurantCustomRepository.findAllByUserId(userId).filter { it.building.id == building.id }
        val customMap = existingCustoms.associateBy { it.restaurant.id }
        val customsToSave = mutableListOf<RestaurantCustomV2>()
        val customsToDelete = mutableListOf<RestaurantCustomV2>()

        existingCustoms
            .filter { it.orderIndex != null && it.restaurant.id !in requestedOrderIds }
            .forEach { custom ->
                custom.orderIndex = null
                if (custom.visible) {
                    customsToDelete.add(custom)
                } else {
                    customsToSave.add(custom)
                }
            }

        requestedOrderIds.forEachIndexed { index, restaurantId ->
            val restaurant = restaurantMap[restaurantId]!!
            val custom = customMap[restaurantId] ?: RestaurantCustomV2(user = user, building = building, restaurant = restaurant)
            custom.orderIndex = index + 1
            customsToSave.add(custom)
        }

        if (customsToDelete.isNotEmpty()) {
            restaurantCustomRepository.deleteAll(customsToDelete)
        }
        if (customsToSave.isNotEmpty()) {
            restaurantCustomRepository.saveAll(customsToSave)
        }

        return RestaurantV2OrderUpdateResponseDto(requestedOrderIds)
    }

    private fun <T> validateDistinctOrder(order: List<T>) {
        if (order.size != order.toSet().size) {
            throw InvalidRestaurantOrderException()
        }
    }
}
