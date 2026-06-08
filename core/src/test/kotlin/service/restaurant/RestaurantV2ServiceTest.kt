package service.restaurant

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.service.RestaurantV2Service
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import java.math.BigDecimal
import kotlin.test.assertNotNull

class RestaurantV2ServiceTest {
    private lateinit var restaurantRepository: RestaurantV2Repository
    private lateinit var userRepository: UserRepository
    private lateinit var restaurantCustomRepository: RestaurantCustomV2Repository
    private lateinit var buildingCustomRepository: BuildingCustomV2Repository
    private lateinit var service: RestaurantV2Service

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        restaurantRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        restaurantCustomRepository = mockk(relaxed = true)
        buildingCustomRepository = mockk(relaxed = true)
        service =
            RestaurantV2Service(
                restaurantRepository,
                userRepository,
                restaurantCustomRepository,
                buildingCustomRepository,
            )
    }

    @Test
    fun `get restaurants returns grouped building response`() {
        // given
        val building =
            testBuilding(
                id = 1,
                number = "109동",
                name = "농협",
                address = "서울대학교 109동",
                latitude = BigDecimal("37.4590000"),
                longitude = BigDecimal("126.9510000"),
            )
        val restaurant = testRestaurant(id = 1, building = building, name = "자하연식당 3층")
        every { restaurantRepository.findAllForList() } returns listOf(restaurant)

        // when
        val result = service.getAllRestaurants()

        // then
        assertNotNull(result)
        Assertions.assertEquals(1, result.count)
        Assertions.assertEquals(1, result.result.size)
        Assertions.assertEquals("109동", result.result[0].buildingNumber)
        Assertions.assertEquals("농협", result.result[0].buildingName)
        Assertions.assertEquals("서울대학교 109동", result.result[0].addr)
        Assertions.assertEquals(BigDecimal("37.4590000"), result.result[0].lat)
        Assertions.assertEquals(BigDecimal("126.9510000"), result.result[0].lng)
        Assertions.assertEquals("자하연식당 3층", result.result[0].restaurants[0].restaurantName)
    }

    @Test
    fun `personalized restaurants are grouped by building order and sorted by display order inside building`() {
        // given
        val userId = 1
        val user = testUser(userId)
        val studentHall = testBuilding(id = 1, number = "63동", name = "학생회관")
        val observatory = testBuilding(id = 2, number = "75-1동", name = "전망대")
        val nh = testBuilding(id = 3, number = "109동", name = "농협")
        val studentHallRestaurant = testRestaurant(id = 1, building = studentHall, name = "학생회관식당", displayOrder = 1)
        val salad = testRestaurant(id = 2, building = observatory, name = "3식당 든든한끼샐러드코너", displayOrder = 2)
        val regular = testRestaurant(id = 3, building = observatory, name = "3식당 일반", displayOrder = 1)
        val jahayeon = testRestaurant(id = 4, building = nh, name = "자하연식당 2층", displayOrder = 1)
        val restaurantCustom =
            RestaurantCustomV2(
                user = user,
                restaurant = salad,
                like = true,
                visible = false,
            )

        every { restaurantRepository.findAllForList() } returns listOf(studentHallRestaurant, regular, salad, jahayeon)
        every { restaurantCustomRepository.findAllByUserId(userId) } returns listOf(restaurantCustom)
        every { buildingCustomRepository.findAllByUserId(userId) } returns
            listOf(
                BuildingCustomV2(user = user, building = observatory, orderIndex = 1),
                BuildingCustomV2(user = user, building = studentHall, orderIndex = 2),
            )

        // when
        val result = service.getAllPersonalizedRestaurants(userId)

        // then
        Assertions.assertEquals(4, result.count)
        Assertions.assertEquals(listOf("75-1동", "63동", "109동"), result.result.map { it.buildingNumber })
        Assertions.assertEquals(
            listOf(regular.id, salad.id),
            result.result[0].restaurants.map { it.id },
        )
        Assertions.assertEquals(true, result.result[0].restaurants[1].liked)
        Assertions.assertEquals(false, result.result[0].restaurants[1].visible)
    }

    private fun testUser(id: Int = 1): User = User(id = id, type = "test", identity = "test", nickname = "test")

    private fun testBuilding(
        id: Int,
        number: String,
        name: String? = null,
        address: String? = null,
        latitude: BigDecimal? = null,
        longitude: BigDecimal? = null,
    ): BuildingV2 =
        BuildingV2(
            id = id,
            number = number,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
        )

    private fun testRestaurant(
        id: Int,
        building: BuildingV2,
        name: String,
        displayOrder: Int = 0,
    ): RestaurantV2 =
        RestaurantV2(
            id = id,
            building = building,
            name = name,
            operatingHours = null,
            ownerId = null,
            displayOrder = displayOrder,
        )
}
