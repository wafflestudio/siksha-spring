package service.menu

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.main.meal.repository.MealMenuV2Repository
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2DetailRow
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2LikedMenuRow
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2MealContextRow
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2MealRow
import siksha.wafflestudio.core.domain.main.menu.data.MenuV2
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAlarmV2Repository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuLikeV2Repository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuV2Repository
import siksha.wafflestudio.core.domain.main.menu.service.MenuV2Service
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.util.Optional

class MenuV2ServiceTest {
    private lateinit var mealMenuRepository: MealMenuV2Repository
    private lateinit var restaurantRepository: RestaurantV2Repository
    private lateinit var buildingRepository: BuildingV2Repository
    private lateinit var buildingCustomRepository: BuildingCustomV2Repository
    private lateinit var restaurantCustomRepository: RestaurantCustomV2Repository
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
        buildingRepository = mockk()
        buildingCustomRepository = mockk()
        restaurantCustomRepository = mockk()
        menuRepository = mockk()
        menuLikeRepository = mockk()
        menuAlarmRepository = mockk()
        userRepository = mockk()
        service =
            MenuV2Service(
                mealMenuRepository,
                restaurantRepository,
                buildingRepository,
                buildingCustomRepository,
                restaurantCustomRepository,
                menuRepository,
                menuLikeRepository,
                menuAlarmRepository,
                userRepository,
            )
    }

    @Test
    fun `web menu list groups menus by date meal type building and restaurant`() {
        val building = BuildingV2(id = 1, number = "301", name = "Building 301", defaultOrder = 1)
        val restaurant = RestaurantV2(id = 10, building = building, name = "Lunch", defaultOrder = 1)
        val date = LocalDate.of(2026, 6, 2)

        every { mealMenuRepository.findMenuRowsByDate(date, date, 0) } returns
            listOf(menuRow(menuId = 100, mealId = 200, mealMenuId = 300, restaurantId = 10, date = date))
        every { restaurantRepository.findAllForList() } returns listOf(restaurant)
        every { buildingRepository.findAllForList() } returns listOf(building)

        val result = service.getMenusWhereDate(date, date, exceptEmpty = true, userId = null)

        assertEquals(1, result.count)
        assertEquals("WEEKDAY", result.result[0].dateType)
        assertEquals(1, result.result[0].lunch.size)
        assertEquals("301", result.result[0].lunch[0].buildingNumber)
        assertEquals(10, result.result[0].lunch[0].restaurants[0].id)
        assertEquals(100, result.result[0].lunch[0].restaurants[0].menus[0].id)
        assertFalse(result.result[0].lunch[0].restaurants[0].menus[0].isLiked)
    }

    @Test
    fun `personal menu list applies building and restaurant custom order and visibility`() {
        val firstBuilding = BuildingV2(id = 1, number = "B1", name = "First", defaultOrder = 1)
        val secondBuilding = BuildingV2(id = 2, number = "B2", name = "Second", defaultOrder = 2)
        val firstRestaurant = RestaurantV2(id = 1, building = firstBuilding, name = "R1", defaultOrder = 1)
        val secondRestaurant = RestaurantV2(id = 2, building = secondBuilding, name = "R2", defaultOrder = 1)
        val thirdRestaurant = RestaurantV2(id = 3, building = secondBuilding, name = "R3", defaultOrder = 2)
        val date = LocalDate.of(2026, 6, 2)

        every { mealMenuRepository.findMenuRowsByDate(date, date, 1) } returns
            listOf(
                menuRow(menuId = 101, mealId = 201, mealMenuId = 301, restaurantId = 1, date = date),
                menuRow(menuId = 102, mealId = 202, mealMenuId = 302, restaurantId = 2, date = date),
                menuRow(menuId = 103, mealId = 203, mealMenuId = 303, restaurantId = 3, date = date),
            )
        every { restaurantRepository.findAllForList() } returns listOf(firstRestaurant, secondRestaurant, thirdRestaurant)
        every { buildingRepository.findAllForList() } returns listOf(firstBuilding, secondBuilding)
        every { buildingCustomRepository.findByUserId(1) } returns
            BuildingCustomV2(
                userId = 1,
                customs = """{"items":{"1":{"order":2,"visible":true},"2":{"order":1,"visible":false}}}""",
            )
        every { restaurantCustomRepository.findByUserId(1) } returns
            RestaurantCustomV2(
                userId = 1,
                customs = """{"items":{"1":{"order":1,"visible":true},"2":{"order":2,"visible":true},"3":{"order":1,"visible":false}}}""",
            )

        val result = service.getMenusWhereDate(date, date, exceptEmpty = true, userId = 1)

        assertEquals(listOf("B2", "B1"), result.result[0].lunch.map { it.buildingNumber })
        assertFalse(result.result[0].lunch[0].visible)
        assertEquals(listOf(3, 2), result.result[0].lunch[0].restaurants.map { it.id })
        assertFalse(result.result[0].lunch[0].restaurants[0].visible)
        assertTrue(result.result[0].lunch[0].restaurants[1].visible)
    }

    @Test
    fun `menu detail uses normalized menu id and includes meal contexts`() {
        every { mealMenuRepository.findMenuDetailById(10, 1) } returns menuDetail(menuId = 10)
        every { mealMenuRepository.findMealContextsByMenuId(10) } returns
            listOf(mealContext(mealMenuId = 30, mealId = 20))

        val result = service.getMenuById(menuId = 10, userId = 1)

        assertEquals(10, result.id)
        assertEquals("Menu", result.nameKr)
        assertEquals("301", result.buildingNumber)
        assertTrue(result.isLiked)
        assertEquals(1, result.meals.size)
        assertEquals(20, result.meals[0].mealId)
        assertEquals("LU", result.meals[0].type)
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
        assertEquals(10, result.id)
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
        assertEquals(10, result.id)
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
        every { buildingCustomRepository.findByUserId(1) } returns null
        every { restaurantCustomRepository.findByUserId(1) } returns null

        val result = service.getMyMenus(userId = 1)

        assertEquals(1, result.count)
        assertEquals("301", result.result[0].buildingNumber)
        assertEquals(1, result.result[0].restaurants[0].id)
        assertEquals(10, result.result[0].restaurants[0].menus[0].id)
        assertTrue(result.result[0].restaurants[0].menus[0].isLiked)
    }

    private fun menuRow(
        menuId: Long,
        mealId: Long,
        mealMenuId: Long,
        restaurantId: Int,
        date: LocalDate,
    ): MenuV2MealRow =
        TestMenuRow(
            mealMenuId = mealMenuId,
            mealId = mealId,
            menuId = menuId,
            restaurantId = restaurantId,
            date = date,
        )

    private fun menuDetail(
        menuId: Long,
        isLiked: Int = 1,
    ): MenuV2DetailRow = TestMenuDetailRow(menuId = menuId, isLiked = isLiked)

    private fun mealContext(
        mealMenuId: Long,
        mealId: Long,
    ): MenuV2MealContextRow = TestMealContextRow(mealMenuId = mealMenuId, mealId = mealId)

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
    ) : MenuV2MealRow {
        override fun getMealMenuId(): Long = mealMenuId
        override fun getMealId(): Long = mealId
        override fun getMenuId(): Long = menuId
        override fun getMenuName(): String = "Menu"
        override fun getOriginalName(): String = "Original Menu"
        override fun getRestaurantId(): Int = restaurantId
        override fun getDate(): LocalDate = date
        override fun getType(): String = "LUNCH"
        override fun getPrice(): Int = 6500
        override fun getNoMeat(): Boolean = false
        override fun getMenuCreatedAt(): Timestamp = timestamp()
        override fun getScore(): Double = 4.5
        override fun getReviewCnt(): Int = 2
        override fun getLikeCnt(): Int = 3
        override fun getIsLiked(): Int = 0
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
        override fun getMenuCreatedAt(): Timestamp = timestamp()
        override fun getScore(): Double = 4.0
        override fun getReviewCnt(): Int = 10
        override fun getLikeCnt(): Int = 5
        override fun getIsLiked(): Int = isLiked
    }

    private data class TestMealContextRow(
        private val mealMenuId: Long,
        private val mealId: Long,
    ) : MenuV2MealContextRow {
        override fun getMealMenuId(): Long = mealMenuId
        override fun getMealId(): Long = mealId
        override fun getDate(): LocalDate = LocalDate.of(2026, 6, 2)
        override fun getType(): String = "LUNCH"
        override fun getPrice(): Int = 6500
        override fun getNoMeat(): Boolean = false
        override fun getOriginalName(): String = "Original Menu"
        override fun getMealCreatedAt(): Timestamp = timestamp()
    }

    private data class TestLikedMenuRow(
        private val menuId: Long,
        private val restaurantId: Int,
    ) : MenuV2LikedMenuRow {
        override fun getMenuId(): Long = menuId
        override fun getMenuName(): String = "Menu"
        override fun getRestaurantId(): Int = restaurantId
        override fun getMenuCreatedAt(): Timestamp = timestamp()
        override fun getScore(): Double = 4.5
        override fun getReviewCnt(): Int = 2
        override fun getLikeCnt(): Int = 3
        override fun getAlarm(): Int = 1
    }

    companion object {
        private fun timestamp(): Timestamp = Timestamp.from(Instant.parse("2026-06-02T00:00:00Z"))
    }
}
