package service.restaurant

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.repository.BuildingCustomV2Repository
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
    private lateinit var restaurantCustomRepository: RestaurantCustomV2Repository
    private lateinit var buildingCustomRepository: BuildingCustomV2Repository
    private lateinit var restaurantLikeRepository: RestaurantLikeV2Repository
    private lateinit var service: RestaurantV2Service

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        restaurantRepository = mockk()
        userRepository = mockk()
        restaurantCustomRepository = mockk()
        buildingCustomRepository = mockk()
        restaurantLikeRepository = mockk()
        service =
            RestaurantV2Service(
                restaurantRepository,
                userRepository,
                restaurantCustomRepository,
                buildingCustomRepository,
                restaurantLikeRepository,
            )
    }

    @Test
    fun `get restaurants returns grouped building response`() {
        // given
        val building =
            BuildingV2(
                id = 1,
                number = "109?",
                name = "??",
                address = "????? 109?",
                latitude = BigDecimal("37.4590000"),
                longitude = BigDecimal("126.9510000"),
                defaultOrder = 1,
            )
        val restaurant =
            RestaurantV2(
                id = 1,
                building = building,
                name = "????? 3?",
                operatingHours = null,
                ownerId = null,
                defaultOrder = 1,
            )
        every { restaurantRepository.findAllForList() } returns listOf(restaurant)

        // when
        val result = service.getAllRestaurants()

        // then
        assertNotNull(result)
        Assertions.assertEquals(1, result.count)
        Assertions.assertEquals(1, result.result.size)
        Assertions.assertEquals("109?", result.result[0].buildingNumber)
        Assertions.assertEquals("??", result.result[0].buildingName)
        Assertions.assertEquals("????? 109?", result.result[0].addr)
        Assertions.assertEquals(BigDecimal("37.4590000"), result.result[0].lat)
        Assertions.assertEquals(BigDecimal("126.9510000"), result.result[0].lng)
        Assertions.assertEquals(1, result.result[0].restaurants[0].id)
    }
}
