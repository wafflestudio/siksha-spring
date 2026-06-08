package siksha.wafflestudio.core.domain.main.restaurant.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.BuildingNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidRestaurantOrderException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class BuildingCustomV2Service(
    private val userRepository: UserRepository,
    private val buildingRepository: BuildingV2Repository,
    private val buildingCustomRepository: BuildingCustomV2Repository,
    private val restaurantRepository: RestaurantV2Repository,
) {
    fun getBuildingOrder(userId: Int): BuildingV2OrderResponseDto {
        val orderedCustoms =
            buildingCustomRepository
                .findAllByUserId(userId)
                .filter { it.orderIndex != null }
                .sortedBy { it.orderIndex }

        return BuildingV2OrderResponseDto(
            buildingOrder = orderedCustoms.map { it.building.number },
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
        val buildingMap =
            buildingRepository
                .findAllByNumberIn(requestedOrderNumberSet)
                .associateBy { it.number }

        if (buildingMap.size != requestedOrderNumberSet.size) {
            throw BuildingNotFoundException()
        }

        val existingCustoms = buildingCustomRepository.findAllByUserId(userId)
        val customMap = existingCustoms.associateBy { it.building.number }

        val customsToSave = mutableListOf<BuildingCustomV2>()

        existingCustoms
            .filter { it.orderIndex != null && it.building.number !in requestedOrderNumberSet }
            .forEach { custom ->
                custom.orderIndex = null
                customsToSave.add(custom)
            }

        requestedOrderNumbers.forEachIndexed { index, buildingNumber ->
            val newOrderIndex = index + 1
            val custom = customMap[buildingNumber] ?: BuildingCustomV2(user = user, building = buildingMap[buildingNumber]!!)

            if (custom.orderIndex != newOrderIndex) {
                custom.orderIndex = newOrderIndex
                customsToSave.add(custom)
            }
        }

        if (customsToSave.isNotEmpty()) {
            buildingCustomRepository.saveAll(customsToSave)
        }

        return BuildingV2OrderUpdateResponseDto(requestedOrderNumbers)
    }

    fun getRestaurantOrderInBuilding(buildingNumber: String): RestaurantV2OrderResponseDto {
        buildingRepository.findByNumber(buildingNumber) ?: throw BuildingNotFoundException()

        val restaurants = restaurantRepository.findAllByBuildingNumber(buildingNumber)

        return RestaurantV2OrderResponseDto(
            restaurantOrder = restaurants.map { it.id },
        )
    }

    private fun <T> validateDistinctOrder(order: List<T>) {
        if (order.size != order.toSet().size) {
            throw InvalidRestaurantOrderException()
        }
    }
}
