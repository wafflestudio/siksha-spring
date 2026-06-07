package siksha.wafflestudio.core.domain.main.restaurant.service

import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFoundException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2LikeResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2VisibleResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class RestaurantV2Service(
    private val restaurantRepository: RestaurantV2Repository,
    private val userRepository: UserRepository,
    private val restaurantCustomRepository: RestaurantCustomV2Repository,
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

        return RestaurantV2ListResponseDto(
            count = resultRestaurants.size,
            result =
                resultRestaurants.map { restaurant ->
                    val custom = customMap[restaurant.id]
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
}
