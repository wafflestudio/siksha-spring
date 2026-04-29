package siksha.wafflestudio.core.domain.main.restaurant.service

import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFoundException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustom
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantLikeResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantOrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantOrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantOrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantVisibleResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomRepository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantRepository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class RestaurantService(
    private val restaurantRepository: RestaurantRepository,
    private val userRepository: UserRepository,
    private val restaurantCustomRepository: RestaurantCustomRepository,
) {
    @Cacheable(value = ["restaurantCache"])
    fun getAllRestaurants(): RestaurantListResponseDto {
        val restaurants = restaurantRepository.findAll()
        return RestaurantListResponseDto(
            count = restaurants.size,
            result =
                restaurants.map { restaurant ->
                    RestaurantResponseDto.from(restaurant)
                },
        )
    }

    fun getAllPersonalizedRestaurants(userId: Int): RestaurantListResponseDto {
        val restaurants = restaurantRepository.findAll()
        val customs = restaurantCustomRepository.findAllByUserId(userId)
        val customMap = customs.associateBy { it.restaurant.id }

        val (orderedRestaurants, unorderedRestaurants) =
            restaurants
                .partition {
                    customMap[it.id]?.orderIndex != null
                }.let { (ordered, unordered) ->
                    ordered.sortedBy { customMap[it.id]!!.orderIndex!! } to unordered
                }

        val resultRestaurants = orderedRestaurants + unorderedRestaurants

        return RestaurantListResponseDto(
            count = resultRestaurants.size,
            result =
                resultRestaurants.map { restaurant ->
                    val custom = customMap[restaurant.id]
                    val liked = custom?.like ?: false
                    val visible = custom?.visible ?: true
                    RestaurantResponseDto.from(restaurant, liked, visible)
                },
        )
    }

    @Transactional
    fun setRestaurantLike(
        userId: Int,
        restaurantId: Int,
        like: Boolean,
    ): RestaurantLikeResponseDto {
        if (restaurantId == null) {
            throw RestaurantNotFoundException()
        }

        val restaurant =
            restaurantRepository
                .findById(restaurantId)
                .orElseThrow { RestaurantNotFoundException() }
        val user =
            userRepository
                .findById(userId)
                .orElseThrow { UserNotFoundException() }

        val custom =
            restaurantCustomRepository.findRestaurantCustomByUserIdAndRestaurantId(userId, restaurant.id)
                ?: RestaurantCustom(user = user, restaurant = restaurant)

        custom.like = like
        val savedCustom = restaurantCustomRepository.save(custom)

        return RestaurantLikeResponseDto(
            restaurantId = restaurantId,
            liked = savedCustom.like,
        )
    }

    @Transactional
    fun setRestaurantVisible(
        userId: Int,
        restaurantId: Int?,
        visible: Boolean,
    ): RestaurantVisibleResponseDto {
        if (restaurantId == null) {
            throw RestaurantNotFoundException()
        }

        val restaurant =
            restaurantRepository
                .findById(restaurantId)
                .orElseThrow { RestaurantNotFoundException() }
        val user =
            userRepository
                .findById(userId)
                .orElseThrow { UserNotFoundException() }

        val custom =
            restaurantCustomRepository.findRestaurantCustomByUserIdAndRestaurantId(userId, restaurant.id)
                ?: RestaurantCustom(user = user, restaurant = restaurant)

        custom.visible = visible
        val savedCustom = restaurantCustomRepository.save(custom)

        return RestaurantVisibleResponseDto(
            restaurantId = restaurantId,
            visible = savedCustom.visible,
        )
    }

    fun getRestaurantOrder(userId: Int): RestaurantOrderResponseDto {
        val orderedCustoms =
            restaurantCustomRepository.findAllByUserId(userId)
                .filter { it.orderIndex != null }
                .sortedBy { it.orderIndex }

        return RestaurantOrderResponseDto(
            restaurantOrder = orderedCustoms.map { it.restaurant.id },
        )
    }

    @Transactional
    fun changeRestaurantOrder(
        userId: Int,
        request: RestaurantOrderUpdateRequestDto,
    ): RestaurantOrderUpdateResponseDto {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val requestedOrderIds = request.order

        val allRestaurants = restaurantRepository.findAll().associateBy { it.id }

        val missingIds = requestedOrderIds.filterNot { allRestaurants.keys.contains(it) }
        if (missingIds.isNotEmpty()) {
            throw RestaurantNotFoundException()
        }

        val existingCustoms = restaurantCustomRepository.findAllByUserId(userId)
        val customMap = existingCustoms.associateBy { it.restaurant.id }
        val toSave = mutableListOf<RestaurantCustom>()

        val requestedOrderIdsSet = requestedOrderIds.toSet()

        requestedOrderIds.forEachIndexed { index, restaurantId ->
            val custom = customMap[restaurantId]
            val newOrderIndex = index + 1

            if (custom != null) {
                if (custom.orderIndex != newOrderIndex) {
                    custom.orderIndex = newOrderIndex
                    toSave.add(custom)
                }
            } else {
                val restaurant = allRestaurants[restaurantId]!!
                toSave.add(
                    RestaurantCustom(user = user, restaurant = restaurant, orderIndex = newOrderIndex),
                )
            }
        }

        existingCustoms.forEach { custom ->
            if (custom.orderIndex != null && !requestedOrderIdsSet.contains(custom.restaurant.id)) {
                custom.orderIndex = null
                toSave.add(custom)
            }
        }

        if (toSave.isNotEmpty()) {
            restaurantCustomRepository.saveAll(toSave)
        }

        return RestaurantOrderUpdateResponseDto(requestedOrderIds)
    }
}
