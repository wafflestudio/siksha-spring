package service.restaurant

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.common.exception.BuildingNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidRestaurantOrderException
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.dto.BuildingV2OrderUpdateRequestDto
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.service.BuildingCustomV2Service
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import java.util.Optional

class BuildingCustomV2ServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var buildingRepository: BuildingV2Repository
    private lateinit var buildingCustomRepository: BuildingCustomV2Repository
    private lateinit var restaurantRepository: RestaurantV2Repository
    private lateinit var service: BuildingCustomV2Service

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        userRepository = mockk(relaxed = true)
        buildingRepository = mockk(relaxed = true)
        buildingCustomRepository = mockk(relaxed = true)
        restaurantRepository = mockk(relaxed = true)
        service =
            BuildingCustomV2Service(
                userRepository,
                buildingRepository,
                buildingCustomRepository,
                restaurantRepository,
            )
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
    fun `get restaurant order in building returns static building scoped order`() {
        // given
        val building = testBuilding(id = 1, number = "301동")
        val regular = testRestaurant(id = 1, building = building, name = "301동식당 일반", displayOrder = 1)
        val cafe = testRestaurant(id = 2, building = building, name = "카페 301", displayOrder = 2)

        every { buildingRepository.findByNumber("301동") } returns building
        every { restaurantRepository.findAllByBuildingNumber("301동") } returns listOf(regular, cafe)

        // when
        val result = service.getRestaurantOrderInBuilding("301동")

        // then
        Assertions.assertEquals(listOf(regular.id, cafe.id), result.restaurantOrder)
    }

    @Test
    fun `get restaurant order in building rejects missing building`() {
        every { buildingRepository.findByNumber("999동") } returns null

        Assertions.assertThrows(BuildingNotFoundException::class.java) {
            service.getRestaurantOrderInBuilding("999동")
        }
    }

    private fun testUser(id: Int = 1): User = User(id = id, type = "test", identity = "test", nickname = "test")

    private fun testBuilding(
        id: Int,
        number: String,
        name: String? = null,
    ): BuildingV2 = BuildingV2(id = id, number = number, name = name)

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
            displayOrder = displayOrder,
        )
}
