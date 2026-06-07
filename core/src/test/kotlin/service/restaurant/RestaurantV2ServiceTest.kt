package service.restaurant

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.CornerV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.repository.CornerCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.CornerV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.service.RestaurantV2Service
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import java.math.BigDecimal
import kotlin.test.assertNotNull

class RestaurantV2ServiceTest {
    private lateinit var cornerRepository: CornerV2Repository
    private lateinit var userRepository: UserRepository
    private lateinit var cornerCustomRepository: CornerCustomV2Repository
    private lateinit var service: RestaurantV2Service

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        cornerRepository = mockk()
        userRepository = mockk()
        cornerCustomRepository = mockk()
        service = RestaurantV2Service(cornerRepository, userRepository, cornerCustomRepository)
    }

    @Test
    fun `get restaurants`() {
        // given
        val building = BuildingV2(id = 1, number = "109동", name = "농협")
        val restaurant =
            RestaurantV2(
                id = 1,
                building = building,
                name = "test",
                address = "test",
                latitude = BigDecimal(0.0),
                longitude = BigDecimal(0.0),
                operatingHours = null,
                ownerId = null,
            )
        val corner = CornerV2(id = 10, restaurant = restaurant, name = "2층")
        every { cornerRepository.findAllActiveForList() } returns listOf(corner)

        // when
        val result = service.getAllRestaurants()

        // then
        assertNotNull(result)
        Assertions.assertEquals(result.result.size, result.count)
        Assertions.assertEquals(10, result.result[0].id)
        Assertions.assertEquals("109동", result.result[0].buildingNumber)
        Assertions.assertEquals("test", result.result[0].restaurantName)
        Assertions.assertEquals("2층", result.result[0].cornerName)
    }
}
