package service.restaurant

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.common.exception.InvalidCustomException
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantLikeV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.service.RestaurantV2Service
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import java.math.BigDecimal
import kotlin.test.assertNotNull

class RestaurantV2ServiceTest {
    private lateinit var restaurantRepository: RestaurantV2Repository
    private lateinit var userRepository: UserRepository
    private lateinit var buildingRepository: BuildingV2Repository
    private lateinit var restaurantCustomRepository: RestaurantCustomV2Repository
    private lateinit var buildingCustomRepository: BuildingCustomV2Repository
    private lateinit var restaurantLikeRepository: RestaurantLikeV2Repository
    private lateinit var service: RestaurantV2Service

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        restaurantRepository = mockk()
        userRepository = mockk()
        buildingRepository = mockk()
        restaurantCustomRepository = mockk()
        buildingCustomRepository = mockk()
        restaurantLikeRepository = mockk()
        service =
            RestaurantV2Service(
                restaurantRepository,
                userRepository,
                buildingRepository,
                restaurantCustomRepository,
                buildingCustomRepository,
                restaurantLikeRepository,
            )
    }

    @Test
    fun `get restaurants returns grouped building response`() {
        val building =
            BuildingV2(
                id = 1,
                number = "109",
                name = "Nonghyup",
                address = "Building 109",
                latitude = BigDecimal("37.4590000"),
                longitude = BigDecimal("126.9510000"),
                defaultOrder = 1,
            )
        val restaurant =
            RestaurantV2(
                id = 1,
                building = building,
                name = "Jahayun 3F",
                operatingHours = null,
                ownerId = null,
                defaultOrder = 1,
            )
        every { restaurantRepository.findAllForList() } returns listOf(restaurant)

        val result = service.getAllRestaurants()

        assertNotNull(result)
        Assertions.assertEquals(1, result.count)
        Assertions.assertEquals(1, result.result.size)
        Assertions.assertEquals("109", result.result[0].buildingNumber)
        Assertions.assertEquals("Nonghyup", result.result[0].buildingName)
        Assertions.assertEquals("Building 109", result.result[0].addr)
        Assertions.assertEquals(BigDecimal("37.4590000"), result.result[0].lat)
        Assertions.assertEquals(BigDecimal("126.9510000"), result.result[0].lng)
        Assertions.assertEquals(1, result.result[0].restaurants[0].id)
    }

    @Test
    fun `personalized restaurants apply json custom order and visibility`() {
        val firstBuilding = BuildingV2(id = 1, number = "B1", name = "First", defaultOrder = 1)
        val secondBuilding = BuildingV2(id = 2, number = "B2", name = "Second", defaultOrder = 2)
        val firstRestaurant = RestaurantV2(id = 1, building = firstBuilding, name = "R1", defaultOrder = 1)
        val secondRestaurant = RestaurantV2(id = 2, building = firstBuilding, name = "R2", defaultOrder = 2)
        val thirdRestaurant = RestaurantV2(id = 3, building = secondBuilding, name = "R3", defaultOrder = 1)
        val fourthRestaurant = RestaurantV2(id = 4, building = secondBuilding, name = "R4", defaultOrder = 2)

        every { restaurantRepository.findAllForList() } returns
            listOf(firstRestaurant, secondRestaurant, thirdRestaurant, fourthRestaurant)
        every { buildingRepository.findAllForList() } returns listOf(firstBuilding, secondBuilding)
        every { buildingCustomRepository.findByUserId(1) } returns
            BuildingCustomV2(
                userId = 1,
                customs = """{"items":{"2":{"order":1,"visible":false},"1":{"order":2,"visible":true}}}""",
            )
        every { restaurantCustomRepository.findByUserId(1) } returns
            RestaurantCustomV2(
                userId = 1,
                customs = """{"items":{"1":{"order":1,"visible":true},"2":{"order":2,"visible":true},"4":{"order":1,"visible":false},"3":{"order":2,"visible":true}}}""",
            )
        every { restaurantLikeRepository.findAllByUserId(1) } returns emptyList()

        val result = service.getAllPersonalizedRestaurants(1)

        Assertions.assertEquals(4, result.count)
        Assertions.assertEquals(listOf("B2", "B1"), result.result.map { it.buildingNumber })
        Assertions.assertEquals(false, result.result[0].visible)
        Assertions.assertEquals(listOf(4, 3), result.result[0].restaurants.map { it.id })
        Assertions.assertEquals(false, result.result[0].restaurants[0].visible)
        Assertions.assertEquals(true, result.result[0].restaurants[1].visible)
    }

    @Test
    fun `personalized restaurants fail when custom json misses required fields`() {
        val building = BuildingV2(id = 1, number = "B1", name = "First", defaultOrder = 1)
        val restaurant = RestaurantV2(id = 1, building = building, name = "R1", defaultOrder = 1)

        every { restaurantRepository.findAllForList() } returns listOf(restaurant)
        every { buildingRepository.findAllForList() } returns listOf(building)
        every { buildingCustomRepository.findByUserId(1) } returns
            BuildingCustomV2(
                userId = 1,
                customs = """{"items":{"1":{"order":1}}}""",
            )

        assertThrows<InvalidCustomException> {
            service.getAllPersonalizedRestaurants(1)
        }
    }

}
