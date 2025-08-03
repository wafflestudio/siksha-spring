package siksha.wafflestudio.core.service.restaurant

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.main.restaurant.data.Restaurant
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantRepository
import siksha.wafflestudio.core.domain.main.restaurant.service.RestaurantService
import kotlin.test.assertNotNull

class RestaurantServiceTest {
    private lateinit var restaurantRepository: RestaurantRepository
    private lateinit var service: RestaurantService

    @BeforeEach
    internal fun setUp() {
        restaurantRepository = mockk()
        service = RestaurantService(restaurantRepository)
        clearAllMocks()
    }

    @Test
    fun `get restaurants`() {
        // given
        val board = Restaurant(
            id = 1,
            code = "test",
            nameKr = "test",
            nameEn = "test",
            addr = "test",
            lat = 0.0,
            lng = 0.0,
            etc = null,
        )
        every { restaurantRepository.findAll() } returns listOf(board)

        // when
        val result = service.getAllRestaurants()

        // when
        assertNotNull(result)
        assertEquals(result.result.size, result.count)
        assertEquals(result.result[0].id, 1)
    }
}
