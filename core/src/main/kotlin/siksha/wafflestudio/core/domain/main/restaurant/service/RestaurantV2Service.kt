package siksha.wafflestudio.core.domain.main.restaurant.service

import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFoundException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.CornerCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2LikeResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ListResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2ResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2VisibleResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.CornerCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.CornerV2Repository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

@Service
class RestaurantV2Service(
    private val cornerRepository: CornerV2Repository,
    private val userRepository: UserRepository,
    private val cornerCustomRepository: CornerCustomV2Repository,
) {
    @Cacheable(value = ["restaurantCache"])
    fun getAllRestaurants(): RestaurantV2ListResponseDto {
        val corners = cornerRepository.findAllActiveForList()
        return RestaurantV2ListResponseDto(
            count = corners.size,
            result =
                corners.map { corner ->
                    RestaurantV2ResponseDto.from(corner)
                },
        )
    }

    fun getAllPersonalizedRestaurants(userId: Int): RestaurantV2ListResponseDto {
        val corners = cornerRepository.findAllActiveForList()
        val customs = cornerCustomRepository.findAllByUserId(userId)
        val customMap = customs.associateBy { it.corner.id }

        val (orderedCorners, unorderedCorners) =
            corners
                .partition {
                    customMap[it.id]?.orderIndex != null
                }.let { (ordered, unordered) ->
                    ordered.sortedBy { customMap[it.id]!!.orderIndex!! } to unordered
                }

        val resultCorners = orderedCorners + unorderedCorners

        return RestaurantV2ListResponseDto(
            count = resultCorners.size,
            result =
                resultCorners.map { corner ->
                    val custom = customMap[corner.id]
                    val liked = custom?.like ?: false
                    val visible = custom?.visible ?: true
                    RestaurantV2ResponseDto.from(corner, liked, visible)
                },
        )
    }

    @Transactional
    fun setRestaurantLike(
        userId: Int,
        restaurantId: Int,
        like: Boolean,
    ): RestaurantV2LikeResponseDto {
        val corner =
            cornerRepository
                .findById(restaurantId)
                .orElseThrow { RestaurantNotFoundException() }
        val user =
            userRepository
                .findById(userId)
                .orElseThrow { UserNotFoundException() }

        val custom =
            cornerCustomRepository.findCornerCustomV2ByUserIdAndCornerId(userId, corner.id)
                ?: CornerCustomV2(user = user, corner = corner)

        custom.like = like
        val savedCustom = cornerCustomRepository.save(custom)

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
        val corner =
            cornerRepository
                .findById(restaurantId)
                .orElseThrow { RestaurantNotFoundException() }
        val user =
            userRepository
                .findById(userId)
                .orElseThrow { UserNotFoundException() }

        val custom =
            cornerCustomRepository.findCornerCustomV2ByUserIdAndCornerId(userId, corner.id)
                ?: CornerCustomV2(user = user, corner = corner)

        custom.visible = visible
        val savedCustom = cornerCustomRepository.save(custom)

        return RestaurantV2VisibleResponseDto(
            restaurantId = restaurantId,
            visible = savedCustom.visible,
        )
    }

    fun getRestaurantOrder(userId: Int): RestaurantV2OrderResponseDto {
        val orderedCustoms =
            cornerCustomRepository
                .findAllByUserId(userId)
                .filter { it.orderIndex != null }
                .sortedBy { it.orderIndex }

        return RestaurantV2OrderResponseDto(
            restaurantOrder = orderedCustoms.map { it.corner.id },
        )
    }

    @Transactional
    fun changeRestaurantOrder(
        userId: Int,
        request: RestaurantV2OrderUpdateRequestDto,
    ): RestaurantV2OrderUpdateResponseDto {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val requestedOrderIds = request.order

        val cornerMap =
            cornerRepository
                .findAllById(requestedOrderIds)
                .associateBy { it.id }

        if (cornerMap.size != requestedOrderIds.toSet().size) {
            throw RestaurantNotFoundException()
        }

        val existingCustoms = cornerCustomRepository.findAllByUserId(userId)
        val customMap = existingCustoms.associateBy { it.corner.id }

        val customsToSave = mutableListOf<CornerCustomV2>()

        existingCustoms
            .filter { it.orderIndex != null && it.corner.id !in requestedOrderIds }
            .forEach { custom ->
                custom.orderIndex = null
                customsToSave.add(custom)
            }

        requestedOrderIds.forEachIndexed { index, cornerId ->
            val newOrderIndex = index + 1
            val custom = customMap[cornerId] ?: CornerCustomV2(user = user, corner = cornerMap[cornerId]!!)

            if (custom.orderIndex != newOrderIndex) {
                custom.orderIndex = newOrderIndex
                customsToSave.add(custom)
            }
        }

        if (customsToSave.isNotEmpty()) {
            cornerCustomRepository.saveAll(customsToSave)
        }

        return RestaurantV2OrderUpdateResponseDto(requestedOrderIds)
    }
}
