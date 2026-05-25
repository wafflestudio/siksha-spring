package service.restaurant

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.service.RestaurantV2Service
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import java.math.BigDecimal
import kotlin.test.assertNotNull

class RestaurantV2ServiceTest {
    private lateinit var restaurantRepository: RestaurantV2Repository
    private lateinit var userRepository: UserRepository
    private lateinit var restaurantCustomRepository: RestaurantCustomV2Repository
    private lateinit var service: RestaurantV2Service

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        restaurantRepository = mockk()
        userRepository = mockk()
        restaurantCustomRepository = mockk()
        service = RestaurantV2Service(restaurantRepository, userRepository, restaurantCustomRepository)
    }

    @Test
    fun `get restaurants`() {
        // given
        val restaurant =
            RestaurantV2(
                id = 1,
                name = "test",
                building = "test",
                address = "test",
                latitude = BigDecimal(0.0),
                longitude = BigDecimal(0.0),
                operatingHours = null,
                ownerId = null,
            )
        every { restaurantRepository.findAll() } returns listOf(restaurant)

        // when
        val result = service.getAllRestaurants()

        // then
        assertNotNull(result)
        Assertions.assertEquals(result.result.size, result.count)
        Assertions.assertEquals(1, result.result[0].id)
    }
}
