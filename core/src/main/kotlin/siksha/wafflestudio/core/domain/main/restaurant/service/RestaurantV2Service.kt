package siksha.wafflestudio.core.domain.main.restaurant.service

import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.BuildingNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidRestaurantOrderException
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFoundException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2LikeResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2VisibleResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class RestaurantV2Service(
    private val restaurantRepository: RestaurantV2Repository,
    private val userRepository: UserRepository,
    private val restaurantCustomRepository: RestaurantCustomV2Repository,
    private val buildingRepository: BuildingV2Repository,
    private val buildingCustomRepository: BuildingCustomV2Repository,
) {
    @Cacheable(value = ["restaurantCache"])
    fun getAllRestaurants(): RestaurantV2ListResponseDto {
        val restaurants = restaurantRepository.findAllForList()
        return RestaurantV2ListResponseDto(
            count = restaurants.size,
            result =
                restaurants.map { restaurant ->
                    RestaurantV2ResponseDto.from(restaurant)
                },
        )
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
                    { restaurant -> buildingCustomMap[restaurant.building.id]?.orderIndex ?: restaurant.building.sortOrder },
                    { restaurant -> restaurant.building.sortOrder },
                    { restaurant -> restaurant.building.id },
                    { restaurant -> if (restaurantCustomMap[restaurant.id]?.orderIndex != null) 0 else 1 },
                    { restaurant -> restaurantCustomMap[restaurant.id]?.orderIndex ?: restaurant.displayOrder },
                    { restaurant -> restaurant.displayOrder },
                    { restaurant -> restaurant.id },
                ),
            )

        return RestaurantV2ListResponseDto(
            count = resultRestaurants.size,
            result =
                resultRestaurants.map { restaurant ->
                    val custom = restaurantCustomMap[restaurant.id]
                    val liked = custom?.like ?: false
                    val visible = custom?.visible ?: true
                    RestaurantV2ResponseDto.from(restaurant, liked, visible)
                },
        )
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

    fun getRestaurantOrder(userId: Int): RestaurantV2OrderResponseDto {
        val orderedCustoms =
            restaurantCustomRepository
                .findAllByUserId(userId)
                .filter { it.orderIndex != null }
                .sortedBy { it.orderIndex }

        return RestaurantV2OrderResponseDto(
            restaurantOrder = orderedCustoms.map { it.restaurant.id },
        )
    }

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
    fun changeRestaurantOrder(
        userId: Int,
        request: RestaurantV2OrderUpdateRequestDto,
    ): RestaurantV2OrderUpdateResponseDto {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val requestedOrderIds = request.order

        val restaurantMap =
            restaurantRepository
                .findAllById(requestedOrderIds)
                .associateBy { it.id }

        if (restaurantMap.size != requestedOrderIds.toSet().size) {
            throw RestaurantNotFoundException()
        }

        val existingCustoms = restaurantCustomRepository.findAllByUserId(userId)
        val customMap = existingCustoms.associateBy { it.restaurant.id }

        val customsToSave = mutableListOf<RestaurantCustomV2>()

        existingCustoms
            .filter { it.orderIndex != null && it.restaurant.id !in requestedOrderIds }
            .forEach { custom ->
                custom.orderIndex = null
                customsToSave.add(custom)
            }

        requestedOrderIds.forEachIndexed { index, restaurantId ->
            val newOrderIndex = index + 1
            val custom = customMap[restaurantId] ?: RestaurantCustomV2(user = user, restaurant = restaurantMap[restaurantId]!!)

            if (custom.orderIndex != newOrderIndex) {
                custom.orderIndex = newOrderIndex
                customsToSave.add(custom)
            }
        }

        if (customsToSave.isNotEmpty()) {
            restaurantCustomRepository.saveAll(customsToSave)
        }

        return RestaurantV2OrderUpdateResponseDto(requestedOrderIds)
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

    fun getRestaurantOrderInBuilding(
        userId: Int,
        buildingNumber: String,
    ): RestaurantV2OrderResponseDto {
        buildingRepository.findByNumber(buildingNumber) ?: throw BuildingNotFoundException()

        val orderedCustoms =
            restaurantCustomRepository
                .findAllByUserIdAndBuildingNumber(userId, buildingNumber)
                .filter { it.orderIndex != null }
                .sortedBy { it.orderIndex }

        return RestaurantV2OrderResponseDto(
            restaurantOrder = orderedCustoms.map { it.restaurant.id },
        )
    }

    @Transactional
    fun changeRestaurantOrderInBuilding(
        userId: Int,
        buildingNumber: String,
        request: RestaurantV2OrderUpdateRequestDto,
    ): RestaurantV2OrderUpdateResponseDto {
        val requestedOrderIds = request.order
        validateDistinctOrder(requestedOrderIds)
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        buildingRepository.findByNumber(buildingNumber) ?: throw BuildingNotFoundException()

        val requestedOrderIdSet = requestedOrderIds.toSet()
        val restaurantMap =
            restaurantRepository
                .findAllByBuildingNumber(buildingNumber)
                .associateBy { it.id }

        if (!restaurantMap.keys.containsAll(requestedOrderIdSet)) {
            throw RestaurantNotFoundException()
        }

        val existingCustoms = restaurantCustomRepository.findAllByUserIdAndBuildingNumber(userId, buildingNumber)
        val customMap = existingCustoms.associateBy { it.restaurant.id }

        val customsToSave = mutableListOf<RestaurantCustomV2>()

        existingCustoms
            .filter { it.orderIndex != null && it.restaurant.id !in requestedOrderIdSet }
            .forEach { custom ->
                custom.orderIndex = null
                customsToSave.add(custom)
            }

        requestedOrderIds.forEachIndexed { index, restaurantId ->
            val newOrderIndex = index + 1
            val custom = customMap[restaurantId] ?: RestaurantCustomV2(user = user, restaurant = restaurantMap[restaurantId]!!)

            if (custom.orderIndex != newOrderIndex) {
                custom.orderIndex = newOrderIndex
                customsToSave.add(custom)
            }
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
