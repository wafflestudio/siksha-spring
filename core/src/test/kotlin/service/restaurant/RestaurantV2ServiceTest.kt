package service.restaurant

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.common.exception.InvalidRestaurantOrderException
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFoundException
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.dto.RestaurantV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.service.RestaurantV2Service
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import java.math.BigDecimal
import java.util.Optional
import kotlin.test.assertNotNull

class RestaurantV2ServiceTest {
    private lateinit var restaurantRepository: RestaurantV2Repository
    private lateinit var userRepository: UserRepository
    private lateinit var restaurantCustomRepository: RestaurantCustomV2Repository
    private lateinit var buildingRepository: BuildingV2Repository
    private lateinit var buildingCustomRepository: BuildingCustomV2Repository
    private lateinit var service: RestaurantV2Service

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        restaurantRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        restaurantCustomRepository = mockk(relaxed = true)
        buildingRepository = mockk(relaxed = true)
        buildingCustomRepository = mockk(relaxed = true)
        service =
            RestaurantV2Service(
                restaurantRepository,
                userRepository,
                restaurantCustomRepository,
                buildingRepository,
                buildingCustomRepository,
            )
    }

    @Test
    fun `get restaurants`() {
        // given
        val building = testBuilding(id = 1, number = "109동", name = "농협")
        val restaurant =
            testRestaurant(
                id = 1,
                building = building,
                name = "자하연식당 3층",
                address = "test",
                latitude = BigDecimal(0.0),
                longitude = BigDecimal(0.0),
            )
        every { restaurantRepository.findAllForList() } returns listOf(restaurant)

        // when
        val result = service.getAllRestaurants()

        // then
        assertNotNull(result)
        Assertions.assertEquals(result.result.size, result.count)
        Assertions.assertEquals(1, result.result[0].id)
        Assertions.assertEquals("109동", result.result[0].buildingNumber)
        Assertions.assertEquals("농협", result.result[0].buildingName)
        Assertions.assertEquals("자하연식당 3층", result.result[0].restaurantName)
    }

    @Test
    fun `personalized restaurants are sorted by building order then restaurant order`() {
        // given
        val userId = 1
        val user = testUser(userId)
        val studentHall = testBuilding(id = 1, number = "63동", name = "학생회관", sortOrder = 1)
        val observatory = testBuilding(id = 2, number = "75-1동", name = "전망대", sortOrder = 2)
        val nh = testBuilding(id = 3, number = "109동", name = "농협", sortOrder = 8)
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
                orderIndex = 1,
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
        Assertions.assertEquals(
            listOf(salad.id, regular.id, studentHallRestaurant.id, jahayeon.id),
            result.result.map { it.id },
        )
        Assertions.assertEquals(true, result.result[0].liked)
        Assertions.assertEquals(false, result.result[0].visible)
    }

    @Test
    fun `get building order returns custom building numbers`() {
        // given
        val userId = 1
        val user = testUser(userId)
        val studentHall = testBuilding(id = 1, number = "63동")
        val observatory = testBuilding(id = 2, number = "75-1동")
        val nh = testBuilding(id = 3, number = "109동")

        every { buildingCustomRepository.findAllByUserId(userId) } returns
            listOf(
                BuildingCustomV2(user = user, building = studentHall, orderIndex = 2),
                BuildingCustomV2(user = user, building = observatory, orderIndex = 1),
                BuildingCustomV2(user = user, building = nh, orderIndex = null),
            )

        // when
        val result = service.getBuildingOrder(userId)

        // then
        Assertions.assertEquals(listOf("75-1동", "63동"), result.buildingOrder)
    }

    @Test
    fun `change building order saves building custom order`() {
        // given
        val userId = 1
        val user = testUser(userId)
        val studentHall = testBuilding(id = 1, number = "63동")
        val observatory = testBuilding(id = 2, number = "75-1동")
        val nh = testBuilding(id = 3, number = "109동")
        val studentHallCustom = BuildingCustomV2(user = user, building = studentHall, orderIndex = 1)
        val nhCustom = BuildingCustomV2(user = user, building = nh, orderIndex = 2)
        val savedCustoms = mutableListOf<BuildingCustomV2>()

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { buildingRepository.findAllByNumberIn(setOf("75-1동", "63동")) } returns listOf(observatory, studentHall)
        every { buildingCustomRepository.findAllByUserId(userId) } returns listOf(studentHallCustom, nhCustom)
        every { buildingCustomRepository.saveAll(any<Iterable<BuildingCustomV2>>()) } answers {
            savedCustoms.addAll(firstArg<Iterable<BuildingCustomV2>>().toList())
            savedCustoms
        }

        // when
        val result = service.changeBuildingOrder(userId, BuildingV2OrderUpdateRequestDto(listOf("75-1동", "63동")))

        // then
        Assertions.assertEquals(listOf("75-1동", "63동"), result.order)
        Assertions.assertEquals(2, studentHallCustom.orderIndex)
        Assertions.assertNull(nhCustom.orderIndex)
        Assertions.assertEquals(1, savedCustoms.single { it.building.number == "75-1동" }.orderIndex)
    }

    @Test
    fun `change building order rejects duplicate building numbers`() {
        Assertions.assertThrows(InvalidRestaurantOrderException::class.java) {
            service.changeBuildingOrder(1, BuildingV2OrderUpdateRequestDto(listOf("63동", "63동")))
        }
    }

    @Test
    fun `change scoped restaurant order only updates restaurants in building`() {
        // given
        val userId = 1
        val user = testUser(userId)
        val building301 = testBuilding(id = 1, number = "301동")
        val building109 = testBuilding(id = 2, number = "109동")
        val faculty = testRestaurant(id = 1, building = building301, name = "301동 1층 교직원전용식당")
        val cafe = testRestaurant(id = 2, building = building301, name = "카페 301")
        val jahayeon = testRestaurant(id = 3, building = building109, name = "자하연식당 2층")
        val facultyCustom = RestaurantCustomV2(user = user, restaurant = faculty, orderIndex = 1)
        val jahayeonCustom = RestaurantCustomV2(user = user, restaurant = jahayeon, orderIndex = 1)
        val savedCustoms = mutableListOf<RestaurantCustomV2>()

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { buildingRepository.findByNumber("301동") } returns building301
        every { restaurantRepository.findAllByBuildingNumber("301동") } returns listOf(faculty, cafe)
        every { restaurantCustomRepository.findAllByUserIdAndBuildingNumber(userId, "301동") } returns listOf(facultyCustom)
        every { restaurantCustomRepository.saveAll(any<Iterable<RestaurantCustomV2>>()) } answers {
            savedCustoms.addAll(firstArg<Iterable<RestaurantCustomV2>>().toList())
            savedCustoms
        }

        // when
        val result = service.changeRestaurantOrderInBuilding(userId, "301동", RestaurantV2OrderUpdateRequestDto(listOf(cafe.id)))

        // then
        Assertions.assertEquals(listOf(cafe.id), result.order)
        Assertions.assertNull(facultyCustom.orderIndex)
        Assertions.assertEquals(1, savedCustoms.single { it.restaurant.id == cafe.id }.orderIndex)
        Assertions.assertEquals(1, jahayeonCustom.orderIndex)
    }

    @Test
    fun `change scoped restaurant order rejects restaurant outside building`() {
        // given
        val userId = 1
        val user = testUser(userId)
        val building301 = testBuilding(id = 1, number = "301동")
        val building109 = testBuilding(id = 2, number = "109동")
        val faculty = testRestaurant(id = 1, building = building301, name = "301동 1층 교직원전용식당")
        val jahayeon = testRestaurant(id = 2, building = building109, name = "자하연식당 2층")

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { buildingRepository.findByNumber("301동") } returns building301
        every { restaurantRepository.findAllByBuildingNumber("301동") } returns listOf(faculty)

        // when, then
        Assertions.assertThrows(RestaurantNotFoundException::class.java) {
            service.changeRestaurantOrderInBuilding(userId, "301동", RestaurantV2OrderUpdateRequestDto(listOf(jahayeon.id)))
        }
    }

    private fun testUser(id: Int = 1): User = User(id = id, type = "test", identity = "test", nickname = "test")

    private fun testBuilding(
        id: Int,
        number: String,
        name: String? = null,
        sortOrder: Int = 0,
    ): BuildingV2 = BuildingV2(id = id, number = number, name = name, sortOrder = sortOrder)

    private fun testRestaurant(
        id: Int,
        building: BuildingV2,
        name: String,
        address: String? = null,
        latitude: BigDecimal? = null,
        longitude: BigDecimal? = null,
        displayOrder: Int = 0,
    ): RestaurantV2 =
        RestaurantV2(
            id = id,
            building = building,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            operatingHours = null,
            ownerId = null,
            displayOrder = displayOrder,
        )
}
