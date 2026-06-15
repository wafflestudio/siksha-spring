package service.menu

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.common.exception.InvalidMealTypeException
import siksha.wafflestudio.core.domain.main.meal.repository.MealMenuV2Repository
import siksha.wafflestudio.core.domain.main.menu.data.MenuV2
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2DetailRow
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2LikedMenuRow
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2MealContextRow
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2MealListRow
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAlarmV2Repository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuLikeV2Repository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuV2Repository
import siksha.wafflestudio.core.domain.main.menu.service.MenuV2Service
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.CustomV2Item
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.service.CustomV2Maps
import siksha.wafflestudio.core.domain.main.restaurant.service.CustomV2Service
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import java.time.LocalDate
import java.util.Optional

class MenuV2ServiceTest {
    private lateinit var mealMenuRepository: MealMenuV2Repository
    private lateinit var restaurantRepository: RestaurantV2Repository
    private lateinit var customService: CustomV2Service
    private lateinit var menuRepository: MenuV2Repository
    private lateinit var menuLikeRepository: MenuLikeV2Repository
    private lateinit var menuAlarmRepository: MenuAlarmV2Repository
    private lateinit var userRepository: UserRepository
    private lateinit var service: MenuV2Service

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        mealMenuRepository = mockk()
        restaurantRepository = mockk()
        customService = mockk()
        menuRepository = mockk()
        menuLikeRepository = mockk()
        menuAlarmRepository = mockk()
        userRepository = mockk()
        service =
            MenuV2Service(
                mealMenuRepository,
                restaurantRepository,
                customService,
                menuRepository,
                menuLikeRepository,
                menuAlarmRepository,
                userRepository,
            )
    }

    @Test
    fun `web menu list returns one date and meal type grouped by building restaurant and meal`() {
        val building = BuildingV2(id = 1, number = "301", name = "Building 301", defaultOrder = 1)
        val restaurant = RestaurantV2(id = 10, building = building, name = "Lunch", defaultOrder = 1)
        val date = LocalDate.of(2026, 6, 2)

        every { mealMenuRepository.findWebMenuRowsByDateAndType(date, "LUNCH") } returns
            listOf(
                menuRow(
                    menuId = 100,
                    mealId = 200,
                    mealMenuId = 300,
                    restaurantId = 10,
                    date = date,
                    originalName = "Original One",
                ),
                menuRow(
                    menuId = 101,
                    mealId = 200,
                    mealMenuId = 301,
                    restaurantId = 10,
                    date = date,
                    originalName = "Original Two",
                ),
            )
        every { restaurantRepository.findAllForList() } returns listOf(restaurant)

        val result = service.getMenusByDateAndTypeForWeb(date = date, type = "LU")

        assertEquals(date, result.date)
        assertEquals("WEEKDAY", result.dateType)
        assertEquals("LU", result.type)
        assertEquals(1, result.buildings.size)
        assertEquals("301", result.buildings[0].buildingNumber)
        val restaurantDto = result.buildings[0].restaurants[0]
        val meal = restaurantDto.meals[0]
        assertEquals(10, restaurantDto.id)
        assertEquals("Lunch", restaurantDto.restaurantName)
        assertEquals(1, restaurantDto.meals.size)
        assertEquals(6500, meal.price)
        assertFalse(meal.noMeat)
        assertEquals(listOf(100L, 101L), meal.menus.map { it.menuId })
        assertEquals(listOf("Original One", "Original Two"), meal.menus.map { it.menuName })
        assertEquals(listOf(null, null), meal.menus.map { it.score })
        assertEquals(listOf(0, 0), meal.menus.map { it.reviewCnt })
        assertEquals(listOf(0, 0), meal.menus.map { it.likeCnt })
        assertEquals(listOf(false, false), meal.menus.map { it.isLiked })
    }

    @Test
    fun `personal menu list applies custom order and filters invisible items`() {
        val firstBuilding = BuildingV2(id = 1, number = "B1", name = "First", defaultOrder = 1)
        val secondBuilding = BuildingV2(id = 2, number = "B2", name = "Second", defaultOrder = 2)
        val thirdBuilding = BuildingV2(id = 3, number = "B3", name = "Hidden", defaultOrder = 3)
        val firstRestaurant = RestaurantV2(id = 1, building = firstBuilding, name = "R1", defaultOrder = 1)
        val secondRestaurant = RestaurantV2(id = 2, building = secondBuilding, name = "R2", defaultOrder = 1)
        val thirdRestaurant = RestaurantV2(id = 3, building = secondBuilding, name = "R3", defaultOrder = 2)
        val fourthRestaurant = RestaurantV2(id = 4, building = thirdBuilding, name = "R4", defaultOrder = 1)
        val date = LocalDate.of(2026, 6, 2)

        every { mealMenuRepository.getMenusByDateAndType(date, "LUNCH", 1) } returns
            listOf(
                menuRow(menuId = 101, mealId = 201, mealMenuId = 301, restaurantId = 1, date = date),
                menuRow(menuId = 102, mealId = 202, mealMenuId = 302, restaurantId = 2, date = date),
                menuRow(menuId = 103, mealId = 203, mealMenuId = 303, restaurantId = 3, date = date),
                menuRow(menuId = 104, mealId = 204, mealMenuId = 304, restaurantId = 4, date = date),
            )
        every { restaurantRepository.findAllForList() } returns
            listOf(firstRestaurant, secondRestaurant, thirdRestaurant, fourthRestaurant)
        every { customService.getCustomMaps(1) } returns
            CustomV2Maps(
                buildingCustomMap =
                    mapOf(
                        1 to CustomV2Item(order = 2, visible = true),
                        2 to CustomV2Item(order = 1, visible = true),
                        3 to CustomV2Item(order = 3, visible = false),
                    ),
                restaurantCustomMap =
                    mapOf(
                        1 to CustomV2Item(order = 1, visible = true),
                        2 to CustomV2Item(order = 2, visible = true),
                        3 to CustomV2Item(order = 1, visible = false),
                        4 to CustomV2Item(order = 1, visible = true),
                    ),
            )

        val result = service.getMenusByDateAndType(date = date, type = "LU", userId = 1)

        assertEquals(listOf("B2", "B1"), result.buildings.map { it.buildingNumber })
        assertEquals(listOf(2), result.buildings[0].restaurants.map { it.id })
        assertEquals(listOf(1), result.buildings[1].restaurants.map { it.id })
    }

    @Test
    fun `menu list excludes empty restaurants and buildings`() {
        val firstBuilding = BuildingV2(id = 1, number = "B1", name = "First", defaultOrder = 1)
        val secondBuilding = BuildingV2(id = 2, number = "B2", name = "Second", defaultOrder = 2)
        val firstRestaurant = RestaurantV2(id = 1, building = firstBuilding, name = "R1", defaultOrder = 1)
        val emptyRestaurant = RestaurantV2(id = 2, building = firstBuilding, name = "R2", defaultOrder = 2)
        val emptyBuildingRestaurant = RestaurantV2(id = 3, building = secondBuilding, name = "R3", defaultOrder = 1)
        val date = LocalDate.of(2026, 6, 2)

        every { mealMenuRepository.findWebMenuRowsByDateAndType(date, "LUNCH") } returns
            listOf(menuRow(menuId = 101, mealId = 201, mealMenuId = 301, restaurantId = 1, date = date))
        every { restaurantRepository.findAllForList() } returns listOf(firstRestaurant, emptyRestaurant, emptyBuildingRestaurant)

        val result = service.getMenusByDateAndTypeForWeb(date = date, type = "LU")

        assertEquals(listOf("B1"), result.buildings.map { it.buildingNumber })
        assertEquals(listOf(1), result.buildings[0].restaurants.map { it.id })
    }

    @Test
    fun `menu list rejects unsupported meal type`() {
        val date = LocalDate.of(2026, 6, 2)

        assertThrows(InvalidMealTypeException::class.java) {
            service.getMenusByDateAndTypeForWeb(date = date, type = "SNACK")
        }
    }

    @Test
    fun `menu detail uses normalized menu id and includes meal contexts`() {
        every { mealMenuRepository.findMenuDetailById(10, 1) } returns menuDetail(menuId = 10)
        every { mealMenuRepository.findMealContextsByMenuId(10) } returns
            listOf(mealContext())

        val result = service.getMenuById(menuId = 10, userId = 1)

        assertEquals(10, result.menuId)
        assertEquals("Menu", result.menuName)
        assertEquals("301", result.buildingNumber)
        assertTrue(result.isLiked)
        assertEquals(1, result.meals.size)
        assertEquals("LU", result.meals[0].type)
        assertEquals("Original Menu", result.meals[0].menuName)
    }

    @Test
    fun `like menu upserts menu like v2 and returns detail`() {
        val building = BuildingV2(id = 1, number = "301", name = "Building", defaultOrder = 1)
        val restaurant = RestaurantV2(id = 1, building = building, name = "Restaurant", defaultOrder = 1)
        every { userRepository.findById(1) } returns Optional.of(testUser())
        every { menuRepository.findById(10) } returns Optional.of(MenuV2(id = 10, restaurant = restaurant, name = "Menu"))
        every { menuLikeRepository.likeMenu(userId = 1, menuId = 10) } just runs
        every { mealMenuRepository.findMenuDetailById(10, 1) } returns menuDetail(menuId = 10)
        every { mealMenuRepository.findMealContextsByMenuId(10) } returns emptyList()

        val result = service.likeMenu(menuId = 10, userId = 1)

        verify { menuLikeRepository.likeMenu(userId = 1, menuId = 10) }
        assertEquals(10, result.menuId)
        assertTrue(result.isLiked)
    }

    @Test
    fun `unlike menu updates menu like v2 to false and returns detail`() {
        val building = BuildingV2(id = 1, number = "301", name = "Building", defaultOrder = 1)
        val restaurant = RestaurantV2(id = 1, building = building, name = "Restaurant", defaultOrder = 1)
        every { userRepository.findById(1) } returns Optional.of(testUser())
        every { menuRepository.findById(10) } returns Optional.of(MenuV2(id = 10, restaurant = restaurant, name = "Menu"))
        every { menuLikeRepository.unlikeMenu(userId = 1, menuId = 10) } just runs
        every { menuAlarmRepository.deleteMenuAlarm(userId = 1, menuId = 10) } just runs
        every { mealMenuRepository.findMenuDetailById(10, 1) } returns menuDetail(menuId = 10, isLiked = 0)
        every { mealMenuRepository.findMealContextsByMenuId(10) } returns emptyList()

        val result = service.unlikeMenu(menuId = 10, userId = 1)

        verify { menuLikeRepository.unlikeMenu(userId = 1, menuId = 10) }
        verify { menuAlarmRepository.deleteMenuAlarm(userId = 1, menuId = 10) }
        assertEquals(10, result.menuId)
        assertFalse(result.isLiked)
    }

    @Test
    fun `menu alarm on requires liked menu and inserts menu alarm v2`() {
        val building = BuildingV2(id = 1, number = "301", name = "Building", defaultOrder = 1)
        val restaurant = RestaurantV2(id = 1, building = building, name = "Restaurant", defaultOrder = 1)
        val menu = MenuV2(id = 10, restaurant = restaurant, name = "Menu")
        every { userRepository.findById(1) } returns Optional.of(testUser())
        every { menuRepository.findById(10) } returns Optional.of(menu)
        every { menuLikeRepository.existsLikedMenu(userId = 1, menuId = 10) } returns true
        every { menuAlarmRepository.existsByUserIdAndMenuId(userId = 1, menuId = 10) } returns false
        every { menuAlarmRepository.postMenuAlarm(userId = 1, menuId = 10) } just runs
        every { mealMenuRepository.findMenuDetailById(10, 1) } returns menuDetail(menuId = 10, isLiked = 1)
        every { mealMenuRepository.findMealContextsByMenuId(10) } returns emptyList()

        val result = service.menuAlarmOn(menuId = 10, userId = 1)

        verify { menuAlarmRepository.postMenuAlarm(userId = 1, menuId = 10) }
        assertTrue(result.alarm)
        assertTrue(result.isLiked)
    }

    @Test
    fun `my menus returns liked menus grouped by building and restaurant`() {
        val building = BuildingV2(id = 1, number = "301", name = "Building", defaultOrder = 1)
        val restaurant = RestaurantV2(id = 1, building = building, name = "Restaurant", defaultOrder = 1)

        every { userRepository.findById(1) } returns Optional.of(testUser())
        every { menuLikeRepository.findLikedMenusByUserId(1) } returns
            listOf(likedMenuRow(menuId = 10, restaurantId = 1))
        every { restaurantRepository.findAllForList() } returns listOf(restaurant)
        every { customService.getCustomMaps(1) } returns null

        val result = service.getMyMenus(userId = 1)

        assertEquals("301", result.buildings[0].buildingNumber)
        assertEquals(1, result.buildings[0].restaurants[0].id)
        assertEquals(
            10,
            result.buildings[0]
                .restaurants[0]
                .menus[0]
                .menuId,
        )
        assertTrue(
            result.buildings[0]
                .restaurants[0]
                .menus[0]
                .isLiked,
        )
    }

    private fun menuRow(
        menuId: Long,
        mealId: Long,
        mealMenuId: Long,
        restaurantId: Int,
        date: LocalDate,
        originalName: String = "Original Menu",
        score: Double? = null,
        reviewCnt: Int = 0,
        likeCnt: Int = 0,
        isLiked: Int = 0,
    ): MenuV2MealListRow =
        TestMenuRow(
            mealMenuId = mealMenuId,
            mealId = mealId,
            menuId = menuId,
            restaurantId = restaurantId,
            date = date,
            originalName = originalName,
            score = score,
            reviewCnt = reviewCnt,
            likeCnt = likeCnt,
            isLiked = isLiked,
        )

    private fun menuDetail(
        menuId: Long,
        isLiked: Int = 1,
    ): MenuV2DetailRow = TestMenuDetailRow(menuId = menuId, isLiked = isLiked)

    private fun mealContext(): MenuV2MealContextRow = TestMealContextRow()

    private fun likedMenuRow(
        menuId: Long,
        restaurantId: Int,
    ): MenuV2LikedMenuRow = TestLikedMenuRow(menuId = menuId, restaurantId = restaurantId)

    private fun testUser(): User =
        User(
            id = 1,
            type = "TEST",
            identity = "test-user",
            nickname = "tester",
        )

    private data class TestMenuRow(
        private val mealMenuId: Long,
        private val mealId: Long,
        private val menuId: Long,
        private val restaurantId: Int,
        private val date: LocalDate,
        private val originalName: String,
        private val score: Double?,
        private val reviewCnt: Int,
        private val likeCnt: Int,
        private val isLiked: Int,
    ) : MenuV2MealListRow {
        override fun getMealMenuId(): Long = mealMenuId

        override fun getMealId(): Long = mealId

        override fun getMenuId(): Long = menuId

        override fun getOriginalName(): String = originalName

        override fun getRestaurantId(): Int = restaurantId

        override fun getDate(): LocalDate = date

        override fun getType(): String = "LUNCH"

        override fun getPrice(): Int = 6500

        override fun getNoMeat(): Boolean = false

        override fun getScore(): Double? = score

        override fun getReviewCnt(): Int = reviewCnt

        override fun getLikeCnt(): Int = likeCnt

        override fun getIsLiked(): Int = isLiked
    }

    private data class TestMenuDetailRow(
        private val menuId: Long,
        private val isLiked: Int,
    ) : MenuV2DetailRow {
        override fun getMenuId(): Long = menuId

        override fun getMenuName(): String = "Menu"

        override fun getRestaurantId(): Int = 1

        override fun getRestaurantName(): String = "Restaurant"

        override fun getBuildingId(): Int = 1

        override fun getBuildingNumber(): String = "301"

        override fun getBuildingName(): String = "Building"

        override fun getScore(): Double = 4.0

        override fun getReviewCnt(): Int = 10

        override fun getLikeCnt(): Int = 5

        override fun getIsLiked(): Int = isLiked
    }

    private class TestMealContextRow : MenuV2MealContextRow {
        override fun getDate(): LocalDate = LocalDate.of(2026, 6, 2)

        override fun getType(): String = "LUNCH"

        override fun getPrice(): Int = 6500

        override fun getNoMeat(): Boolean = false

        override fun getOriginalName(): String = "Original Menu"
    }

    private data class TestLikedMenuRow(
        private val menuId: Long,
        private val restaurantId: Int,
    ) : MenuV2LikedMenuRow {
        override fun getMenuId(): Long = menuId

        override fun getMenuName(): String = "Menu"

        override fun getRestaurantId(): Int = restaurantId

        override fun getScore(): Double = 4.5

        override fun getReviewCnt(): Int = 2

        override fun getLikeCnt(): Int = 3

        override fun getAlarm(): Int = 1
    }
}
