package siksha.wafflestudio.core.domain.main.restaurant.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.InvalidCustomException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2Item
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.dto.CustomV2BuildingItemDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.CustomV2ResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.CustomV2RestaurantItemDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.CustomV2UpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.CustomV2UpdateResponseDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.CustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.user.repository.UserRepository

data class CustomV2Maps(
    val buildingCustomMap: Map<Int, CustomV2Item>,
    val restaurantCustomMap: Map<Int, CustomV2Item>,
)

@Service
class CustomV2Service(
    private val userRepository: UserRepository,
    private val buildingRepository: BuildingV2Repository,
    private val restaurantRepository: RestaurantV2Repository,
    private val customRepository: CustomV2Repository,
) {
    private val objectMapper =
        jacksonObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun getCustoms(userId: Int): CustomV2ResponseDto {
        val buildings = buildingRepository.findAllForList()
        val restaurants = restaurantRepository.findAllForList()
        val maps =
            customRepository
                .findByUserId(userId)
                ?.let { validateAndBuildMaps(parse(it.customs).customs, buildings, restaurants) }

        return CustomV2ResponseDto(customs = toCustomDtos(buildings, restaurants, maps))
    }

    @Transactional
    fun updateCustoms(
        userId: Int,
        request: CustomV2UpdateRequestDto,
    ): CustomV2UpdateResponseDto {
        userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val buildings = buildingRepository.findAllForList()
        val restaurants = restaurantRepository.findAllForList()
        val maps = validateAndBuildMaps(request.customs, buildings, restaurants)
        val normalized = toCustomDtos(buildings, restaurants, maps)
        val customs = stringify(CustomV2ResponseDto(customs = normalized))
        val entity = customRepository.findByUserId(userId) ?: CustomV2(userId = userId)
        entity.customs = customs
        customRepository.save(entity)

        return CustomV2UpdateResponseDto(customs = normalized)
    }

    fun getCustomMaps(userId: Int): CustomV2Maps? {
        val custom = customRepository.findByUserId(userId) ?: return null
        val buildings = buildingRepository.findAllForList()
        val restaurants = restaurantRepository.findAllForList()
        return validateAndBuildMaps(parse(custom.customs).customs, buildings, restaurants)
    }

    private fun parse(customs: String?): CustomV2ResponseDto =
        if (customs.isNullOrBlank()) {
            CustomV2ResponseDto(customs = emptyList())
        } else {
            runCatching { objectMapper.readValue<CustomV2ResponseDto>(customs) }
                .getOrElse { throw InvalidCustomException() }
        }

    private fun stringify(response: CustomV2ResponseDto): String = objectMapper.writeValueAsString(response)

    private fun validateAndBuildMaps(
        requestCustoms: List<CustomV2BuildingItemDto>,
        buildings: List<BuildingV2>,
        restaurants: List<RestaurantV2>,
    ): CustomV2Maps {
        val buildingMap = buildings.associateBy { it.number }
        val requestBuildingMap = requestCustoms.associateBy { it.buildingNumber }

        if (requestBuildingMap.size != requestCustoms.size || requestBuildingMap.keys != buildingMap.keys) {
            throw InvalidCustomException()
        }
        validateDenseOrder(requestCustoms.map { it.order })

        val restaurantsByBuilding = restaurants.groupBy { it.building.id }
        val buildingCustomMap = linkedMapOf<Int, CustomV2Item>()
        val restaurantCustomMap = linkedMapOf<Int, CustomV2Item>()

        requestCustoms.forEach { buildingCustom ->
            val building = buildingMap[buildingCustom.buildingNumber] ?: throw InvalidCustomException()
            buildingCustomMap[building.id] = CustomV2Item(order = buildingCustom.order, visible = buildingCustom.visible)

            val restaurantsInBuilding = restaurantsByBuilding[building.id].orEmpty()
            val restaurantMap = restaurantsInBuilding.associateBy { it.name }
            val requestRestaurantMap = buildingCustom.restaurants.associateBy { it.name }

            if (requestRestaurantMap.size != buildingCustom.restaurants.size || requestRestaurantMap.keys != restaurantMap.keys) {
                throw InvalidCustomException()
            }
            validateDenseOrder(buildingCustom.restaurants.map { it.order })

            buildingCustom.restaurants.forEach { restaurantCustom ->
                val restaurant = restaurantMap[restaurantCustom.name] ?: throw InvalidCustomException()
                restaurantCustomMap[restaurant.id] =
                    CustomV2Item(order = restaurantCustom.order, visible = restaurantCustom.visible)
            }
        }

        return CustomV2Maps(
            buildingCustomMap = buildingCustomMap,
            restaurantCustomMap = restaurantCustomMap,
        )
    }

    private fun toCustomDtos(
        buildings: List<BuildingV2>,
        restaurants: List<RestaurantV2>,
        maps: CustomV2Maps?,
    ): List<CustomV2BuildingItemDto> =
        buildings
            .map { building ->
                val restaurantsInBuilding = restaurants.filter { it.building.id == building.id }
                val buildingItem = maps?.buildingCustomMap?.get(building.id)
                val restaurantDtos =
                    restaurantsInBuilding
                        .map { restaurant ->
                            val restaurantItem = maps?.restaurantCustomMap?.get(restaurant.id)
                            CustomV2RestaurantItemDto(
                                name = restaurant.name,
                                order = restaurantItem?.order ?: restaurant.defaultOrder,
                                visible = restaurantItem?.visible ?: true,
                            )
                        }.sortedBy { it.order }
                CustomV2BuildingItemDto(
                    buildingNumber = building.number,
                    order = buildingItem?.order ?: building.defaultOrder,
                    visible = buildingItem?.visible ?: true,
                    restaurants = restaurantDtos,
                )
            }.sortedBy { it.order }

    private fun validateDenseOrder(orders: List<Int>) {
        if (orders.isEmpty()) {
            return
        }
        if (orders.toSet() != (1..orders.size).toSet()) {
            throw InvalidCustomException()
        }
    }
}
