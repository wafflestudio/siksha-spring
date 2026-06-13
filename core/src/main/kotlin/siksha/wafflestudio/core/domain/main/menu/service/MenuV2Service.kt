package siksha.wafflestudio.core.domain.main.menu.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import siksha.wafflestudio.core.domain.common.exception.InvalidCustomException
import siksha.wafflestudio.core.domain.common.exception.MenuAlarmAlreadyExistsException
import siksha.wafflestudio.core.domain.common.exception.MenuAlarmException
import siksha.wafflestudio.core.domain.common.exception.MenuNotFoundException
import siksha.wafflestudio.core.domain.common.exception.MenuNotLikedException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.main.meal.repository.MealMenuV2Repository
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2AlarmDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2BuildingInListDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2DateWithTypeDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2DetailsDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2InListDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2LikedBuildingDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2LikedListResponseDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2LikedMenuDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2LikedMenuRow
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2LikedRestaurantDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2ListResponseDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2MealContextDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2MealRow
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2RestaurantInListDto
import siksha.wafflestudio.core.domain.main.menu.dto.toMealTypeCode
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAlarmV2Repository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuLikeV2Repository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2Document
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2Item
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2Json
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.data.itemOf
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import java.io.InputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class MenuV2Service(
    private val mealMenuRepository: MealMenuV2Repository,
    private val restaurantRepository: RestaurantV2Repository,
    private val buildingRepository: BuildingV2Repository,
    private val buildingCustomRepository: BuildingCustomV2Repository,
    private val restaurantCustomRepository: RestaurantCustomV2Repository,
    private val menuRepository: MenuV2Repository,
    private val menuLikeRepository: MenuLikeV2Repository,
    private val menuAlarmRepository: MenuAlarmV2Repository,
    private val userRepository: UserRepository,
) {
    private val holidays: Set<LocalDate> = loadHolidays()

    fun getMenusWhereDate(
        startDate: LocalDate,
        endDate: LocalDate,
        exceptEmpty: Boolean,
        userId: Int?,
    ): MenuV2ListResponseDto {
        if (startDate.isAfter(endDate)) {
            return MenuV2ListResponseDto(count = 0, result = emptyList())
        }

        val targetUserId = userId ?: 0
        val rows = mealMenuRepository.findMenuRowsByDate(startDate, endDate, targetUserId)
        if (rows.isEmpty()) {
            return MenuV2ListResponseDto(count = 0, result = emptyList())
        }

        val restaurants = restaurantRepository.findAllForList()
        val buildingCustomMap = userId?.let { loadBuildingCustomMap(it) }
        val restaurantCustomMap = userId?.let { loadRestaurantCustomMap(it, restaurants) }
        val orderedRestaurants = restaurants.sortedForMenu(buildingCustomMap, restaurantCustomMap)
        val rowsByDateAndType = rows.groupBy { it.getDate() to it.getType().toMealTypeCode() }

        val result =
            datesBetween(startDate, endDate).map { date ->
                MenuV2DateWithTypeDto(
                    date = date,
                    dateType = getDateType(date),
                    breakfast =
                        buildBuildings(
                            restaurants = orderedRestaurants,
                            rows = rowsByDateAndType[date to "BR"].orEmpty(),
                            buildingCustomMap = buildingCustomMap,
                            restaurantCustomMap = restaurantCustomMap,
                            exceptEmpty = exceptEmpty,
                        ),
                    lunch =
                        buildBuildings(
                            restaurants = orderedRestaurants,
                            rows = rowsByDateAndType[date to "LU"].orEmpty(),
                            buildingCustomMap = buildingCustomMap,
                            restaurantCustomMap = restaurantCustomMap,
                            exceptEmpty = exceptEmpty,
                        ),
                    dinner =
                        buildBuildings(
                            restaurants = orderedRestaurants,
                            rows = rowsByDateAndType[date to "DN"].orEmpty(),
                            buildingCustomMap = buildingCustomMap,
                            restaurantCustomMap = restaurantCustomMap,
                            exceptEmpty = exceptEmpty,
                        ),
                )
            }

        return MenuV2ListResponseDto(count = result.size, result = result)
    }

    fun getMenuById(
        menuId: Long,
        userId: Int?,
    ): MenuV2DetailsDto {
        val targetUserId = userId ?: 0
        val detail = mealMenuRepository.findMenuDetailById(menuId, targetUserId) ?: throw MenuNotFoundException()
        val mealContexts = mealMenuRepository.findMealContextsByMenuId(menuId).map(MenuV2MealContextDto::from)
        return MenuV2DetailsDto.from(detail, mealContexts)
    }

    @Transactional
    fun likeMenu(
        menuId: Long,
        userId: Int,
    ): MenuV2DetailsDto {
        ensureUserAndMenuExist(userId, menuId)
        menuLikeRepository.likeMenu(userId = userId, menuId = menuId)
        return getMenuById(menuId = menuId, userId = userId)
    }

    @Transactional
    fun unlikeMenu(
        menuId: Long,
        userId: Int,
    ): MenuV2DetailsDto {
        ensureUserAndMenuExist(userId, menuId)
        menuLikeRepository.unlikeMenu(userId = userId, menuId = menuId)
        menuAlarmRepository.deleteMenuAlarm(userId = userId, menuId = menuId)
        return getMenuById(menuId = menuId, userId = userId)
    }

    fun getMyMenus(userId: Int): MenuV2LikedListResponseDto {
        userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val rows = menuLikeRepository.findLikedMenusByUserId(userId)
        if (rows.isEmpty()) {
            return MenuV2LikedListResponseDto(count = 0, result = emptyList())
        }

        val restaurants = restaurantRepository.findAllForList()
        val buildingCustomMap = loadBuildingCustomMap(userId)
        val restaurantCustomMap = loadRestaurantCustomMap(userId, restaurants)
        val orderedRestaurants = restaurants.sortedForMenu(buildingCustomMap, restaurantCustomMap)
        val rowsByRestaurant = rows.groupBy { it.getRestaurantId() }

        val result = buildLikedBuildings(orderedRestaurants, rowsByRestaurant, buildingCustomMap, restaurantCustomMap)
        return MenuV2LikedListResponseDto(count = rows.size, result = result)
    }

    @Transactional
    fun menuAlarmOn(
        menuId: Long,
        userId: Int,
    ): MenuV2AlarmDto {
        val (_, menu) = ensureUserAndMenuExist(userId, menuId)
        if (!menuLikeRepository.existsLikedMenu(userId = userId, menuId = menuId)) {
            throw MenuNotLikedException()
        }
        if (menuAlarmRepository.existsByUserIdAndMenuId(userId = userId, menuId = menuId)) {
            throw MenuAlarmAlreadyExistsException()
        }

        try {
            menuAlarmRepository.postMenuAlarm(userId = userId, menuId = menuId)
        } catch (e: Exception) {
            throw MenuAlarmException()
        }

        return MenuV2AlarmDto.from(getMenuById(menuId = menu.id, userId = userId), alarm = true)
    }

    @Transactional
    fun menuAlarmOff(
        menuId: Long,
        userId: Int,
    ): MenuV2AlarmDto {
        val (_, menu) = ensureUserAndMenuExist(userId, menuId)
        if (!menuLikeRepository.existsLikedMenu(userId = userId, menuId = menuId)) {
            throw MenuNotLikedException()
        }

        try {
            menuAlarmRepository.deleteMenuAlarm(userId = userId, menuId = menuId)
        } catch (e: Exception) {
            throw MenuAlarmException()
        }

        return MenuV2AlarmDto.from(getMenuById(menuId = menu.id, userId = userId), alarm = false)
    }

    @Transactional
    fun menuAlarmOffAll(userId: Int) {
        userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        try {
            menuAlarmRepository.deleteMenuAlarmByUserId(userId)
        } catch (e: Exception) {
            throw MenuAlarmException()
        }
    }

    @Transactional
    fun menuAlarmOnAll(userId: Int) {
        userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        try {
            menuAlarmRepository.postAllLikedMenuAlarms(userId)
        } catch (e: Exception) {
            throw MenuAlarmException()
        }
    }

    private fun ensureUserAndMenuExist(
        userId: Int,
        menuId: Long,
    ) =
        userRepository.findById(userId).orElseThrow { UserNotFoundException() } to
            menuRepository.findById(menuId).orElseThrow { MenuNotFoundException() }

    private fun buildLikedBuildings(
        restaurants: List<RestaurantV2>,
        rowsByRestaurant: Map<Int, List<MenuV2LikedMenuRow>>,
        buildingCustomMap: Map<Int, CustomV2Item>?,
        restaurantCustomMap: Map<Int, CustomV2Item>?,
    ): List<MenuV2LikedBuildingDto> =
        restaurants
            .groupBy { it.building.id }
            .values
            .mapNotNull { restaurantsInBuilding ->
                val building = restaurantsInBuilding.first().building
                val restaurantDtos =
                    restaurantsInBuilding.mapNotNull { restaurant ->
                        val menuDtos = rowsByRestaurant[restaurant.id].orEmpty().map(MenuV2LikedMenuDto::from)
                        if (menuDtos.isEmpty()) {
                            null
                        } else {
                            MenuV2LikedRestaurantDto(
                                id = restaurant.id,
                                code = restaurant.name,
                                nameKr = restaurant.name,
                                restaurantName = restaurant.name,
                                visible = restaurantCustomMap?.get(restaurant.id)?.visible ?: true,
                                menus = menuDtos,
                            )
                        }
                    }
                if (restaurantDtos.isEmpty()) {
                    null
                } else {
                    MenuV2LikedBuildingDto(
                        buildingNumber = building.number,
                        buildingName = building.name,
                        addr = building.address,
                        lat = building.latitude,
                        lng = building.longitude,
                        visible = buildingCustomMap?.get(building.id)?.visible ?: true,
                        restaurants = restaurantDtos,
                    )
                }
            }

    private fun buildBuildings(
        restaurants: List<RestaurantV2>,
        rows: List<MenuV2MealRow>,
        buildingCustomMap: Map<Int, CustomV2Item>?,
        restaurantCustomMap: Map<Int, CustomV2Item>?,
        exceptEmpty: Boolean,
    ): List<MenuV2BuildingInListDto> {
        val rowsByRestaurant = rows.groupBy { it.getRestaurantId() }
        return restaurants
            .groupBy { it.building.id }
            .values
            .mapNotNull { restaurantsInBuilding ->
                val building = restaurantsInBuilding.first().building
                val restaurantDtos =
                    restaurantsInBuilding.mapNotNull { restaurant ->
                        val menuDtos = rowsByRestaurant[restaurant.id].orEmpty().map(MenuV2InListDto::from)
                        if (exceptEmpty && menuDtos.isEmpty()) {
                            null
                        } else {
                            MenuV2RestaurantInListDto(
                                id = restaurant.id,
                                code = restaurant.name,
                                nameKr = restaurant.name,
                                restaurantName = restaurant.name,
                                visible = restaurantCustomMap?.get(restaurant.id)?.visible ?: true,
                                menus = menuDtos,
                            )
                        }
                    }
                if (exceptEmpty && restaurantDtos.isEmpty()) {
                    null
                } else {
                    MenuV2BuildingInListDto(
                        buildingNumber = building.number,
                        buildingName = building.name,
                        addr = building.address,
                        lat = building.latitude,
                        lng = building.longitude,
                        visible = buildingCustomMap?.get(building.id)?.visible ?: true,
                        restaurants = restaurantDtos,
                    )
                }
            }
    }

    private fun List<RestaurantV2>.sortedForMenu(
        buildingCustomMap: Map<Int, CustomV2Item>?,
        restaurantCustomMap: Map<Int, CustomV2Item>?,
    ): List<RestaurantV2> =
        sortedWith(
            compareBy(
                { restaurant -> buildingCustomMap?.get(restaurant.building.id)?.order ?: restaurant.building.defaultOrder },
                { restaurant -> restaurant.building.id },
                { restaurant -> restaurantCustomMap?.get(restaurant.id)?.order ?: restaurant.defaultOrder },
                { restaurant -> restaurant.id },
            ),
        )

    private fun loadBuildingCustomMap(userId: Int): Map<Int, CustomV2Item>? {
        val custom = buildingCustomRepository.findByUserId(userId) ?: return null
        val buildings = buildingRepository.findAllForList()
        return requireCompleteBuildingDocument(CustomV2Json.parse(custom.customs), buildings)
    }

    private fun loadRestaurantCustomMap(
        userId: Int,
        restaurants: List<RestaurantV2>,
    ): Map<Int, CustomV2Item>? =
        restaurantCustomRepository.findByUserId(userId)
            ?.let { requireCompleteRestaurantDocument(CustomV2Json.parse(it.customs), restaurants) }

    private fun requireCompleteBuildingDocument(
        document: CustomV2Document,
        buildings: List<BuildingV2>,
    ): Map<Int, CustomV2Item> {
        val expectedIds = buildings.map { it.id }.toSet()
        val itemMap = requireCompleteItems(document, expectedIds)
        validateDenseOrder(itemMap.values.map { it.order!! })
        return itemMap
    }

    private fun requireCompleteRestaurantDocument(
        document: CustomV2Document,
        restaurants: List<RestaurantV2>,
    ): Map<Int, CustomV2Item> {
        val expectedIds = restaurants.map { it.id }.toSet()
        val itemMap = requireCompleteItems(document, expectedIds)
        restaurants.groupBy { it.building.id }.values.forEach { restaurantsInBuilding ->
            validateDenseOrder(restaurantsInBuilding.map { itemMap[it.id]!!.order!! })
        }
        return itemMap
    }

    private fun requireCompleteItems(
        document: CustomV2Document,
        expectedIds: Set<Int>,
    ): Map<Int, CustomV2Item> {
        val actualIds = document.items.keys.map { it.toIntOrNull() ?: throw InvalidCustomException() }.toSet()
        if (actualIds != expectedIds) {
            throw InvalidCustomException()
        }
        return expectedIds.associateWith { id ->
            document.itemOf(id)?.takeIf { it.isComplete() } ?: throw InvalidCustomException()
        }
    }

    private fun validateDenseOrder(orders: List<Int>) {
        if (orders.toSet() != (1..orders.size).toSet()) {
            throw InvalidCustomException()
        }
    }

    private fun datesBetween(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            dates.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }
        return dates
    }

    private fun loadHolidays(): Set<LocalDate> {
        val resourcePath = "/2025.json"
        val stream: InputStream =
            this::class.java.getResourceAsStream(resourcePath)
                ?: return emptySet()
        val raw: Map<String, List<String>> = jacksonObjectMapper().readValue(stream)
        return raw.keys.map { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }.toSet()
    }

    private fun isHoliday(date: LocalDate): Boolean = holidays.contains(date)

    private fun getDateType(date: LocalDate): String =
        when {
            isHoliday(date) -> "HOLIDAY"
            date.dayOfWeek == DayOfWeek.SUNDAY -> "HOLIDAY"
            date.dayOfWeek == DayOfWeek.SATURDAY -> "SATURDAY"
            else -> "WEEKDAY"
        }
}
