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
        restaurantId: Int,
        visible: Boolean,
    ): RestaurantVisibleResponseDto {
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
            restaurantCustomRepository
                .findAllByUserId(userId)
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

        val restaurantMap =
            restaurantRepository
                .findAllById(requestedOrderIds)
                .associateBy { it.id }

        if (restaurantMap.size != requestedOrderIds.toSet().size) {
            throw RestaurantNotFoundException()
        }

        val existingCustoms = restaurantCustomRepository.findAllByUserId(userId)
        val customMap = existingCustoms.associateBy { it.restaurant.id }

        val customsToSave = mutableListOf<RestaurantCustom>()

        existingCustoms
            .filter { it.orderIndex != null && it.restaurant.id !in requestedOrderIds }
            .forEach { custom ->
                custom.orderIndex = null
                customsToSave.add(custom)
            }

        requestedOrderIds.forEachIndexed { index, restaurantId ->
            val newOrderIndex = index + 1
            val custom = customMap[restaurantId] ?: RestaurantCustom(user = user, restaurant = restaurantMap[restaurantId]!!)

            if (custom.orderIndex != newOrderIndex) {
                custom.orderIndex = newOrderIndex
                customsToSave.add(custom)
            }
        }

        if (customsToSave.isNotEmpty()) {
            restaurantCustomRepository.saveAll(customsToSave)
        }

        return RestaurantOrderUpdateResponseDto(requestedOrderIds)
    }
}
