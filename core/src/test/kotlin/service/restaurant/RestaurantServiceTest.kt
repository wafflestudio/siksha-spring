package siksha.wafflestudio.core.domain.main.restaurant.service

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.main.restaurant.data.Restaurant
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantCustomRepository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantRepository
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import kotlin.test.assertNotNull

class RestaurantServiceTest {
    private lateinit var restaurantRepository: RestaurantRepository
    private lateinit var userRepository: UserRepository
    private lateinit var restaurantCustomRepository: RestaurantCustomRepository
    private lateinit var service: RestaurantService

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        restaurantRepository = mockk()
        userRepository = mockk()
        restaurantCustomRepository = mockk()
        service = RestaurantService(restaurantRepository, userRepository, restaurantCustomRepository)
    }

    @Test
    fun `get restaurants`() {
        // given
        val restaurant =
            Restaurant(
                id = 1,
                code = "test",
                nameKr = "test",
                nameEn = "test",
                addr = "test",
                lat = 0.0,
                lng = 0.0,
                etc = null,
            )
        every { restaurantRepository.findAll() } returns listOf(restaurant)

        // when
        val result = service.getAllRestaurants()

        // then
        assertNotNull(result)
        assertEquals(result.result.size, result.count)
        assertEquals(1, result.result[0].id)
    }
}
