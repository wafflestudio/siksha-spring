package siksha.wafflestudio.core.domain.main.restaurant.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFoundException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.community.comment.repository.CommentRepository
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantLike
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantLikeResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantLikeRepository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantRepository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class RestaurantService(
    private val restaurantRepository: RestaurantRepository,
    private val userRepository: UserRepository,
    private val restaurantLikeRepository: RestaurantLikeRepository,
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
}
