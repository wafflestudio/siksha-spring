package siksha.wafflestudio.core.domain.main.restaurant.service

import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFoundException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantLike
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantOrder
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantLikeResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantOrderRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantOrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantLikeRepository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantOrderRepository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantRepository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class RestaurantService(
    private val restaurantRepository: RestaurantRepository,
    private val userRepository: UserRepository,
    private val restaurantLikeRepository: RestaurantLikeRepository,
    private val restaurantOrderRepository: RestaurantOrderRepository
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

    fun getAllPersonalizedRestaurants(
        userId: Int,
    ): RestaurantListResponseDto {
        val restaurants = restaurantRepository.findAll()
        val restaurantOrder = restaurantOrderRepository.findRestaurantOrderByUserId(userId)?.orderId



        return RestaurantListResponseDto(
            count = restaurants.size,
            result = restaurants.map { restaurant ->
                val liked = restaurantLikeRepository.existsRestaurantLikeByUserIdAndRestaurantId(userId, restaurant.id)
                RestaurantResponseDto.personalizedFrom(restaurant, liked)
            },
        )
    }

    fun likeRestaurant(userId: Int, restaurantId: Int?): RestaurantLikeResponseDto {
        if(restaurantId == null) {
            throw RestaurantNotFoundException()
        }

        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { RestaurantNotFoundException() }
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }

        val existingLike = restaurantLikeRepository.findRestaurantLikeByUserIdAndRestaurantId(userId, restaurantId)

        if(existingLike == null) {
            val newLike = RestaurantLike(
                user = user,
                restaurant = restaurant,
            )
            restaurantLikeRepository.save(newLike)

            return RestaurantLikeResponseDto(
                restaurantId = restaurantId,
                liked = true,
            )
        }
        else {
            restaurantLikeRepository.delete(existingLike)
            return RestaurantLikeResponseDto(
                restaurantId = restaurantId,
                liked = false,
            )
        }
    }

    fun getRestaurantOrder(
        userId: Int,
    ): RestaurantOrderResponseDto {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val order = restaurantOrderRepository.findRestaurantOrderByUserId(userId)

        if(order == null) {
            val newOrder = RestaurantOrder(
                user = user,
                orderId = restaurantRepository.findAll().map { it.id }
            )
            restaurantOrderRepository.save(newOrder)

            return RestaurantOrderResponseDto(
                restaurantOrder = newOrder.orderId
            )
        }
        else {
            return RestaurantOrderResponseDto(
                restaurantOrder = order.orderId
            )
        }
    }

    @Transactional
    fun changeRestaurantOrder(
        userId: Int,
        requestedOrder: RestaurantOrderRequestDto,
    ) {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val order = requestedOrder.restaurantOrder

        // 1. 순서에 있는 식당이 모두 존재하는지 확인
        val allRestaurantIds = restaurantRepository.findAll().map { it.id }.toSet()
        if(!order.all { allRestaurantIds.contains(it) }) {
            throw RestaurantNotFoundException()
        }

        // 2. 기존 순서가 존재하는지 확인
        val existingOrder = restaurantOrderRepository.findRestaurantOrderByUserId(userId)
        if(existingOrder == null) {
            val newOrder = RestaurantOrder(
                user = user,
                orderId = order
            )
            restaurantOrderRepository.save(newOrder)
        }
        else {
            existingOrder.orderId = order
            restaurantOrderRepository.save(existingOrder)
        }
    }
}
