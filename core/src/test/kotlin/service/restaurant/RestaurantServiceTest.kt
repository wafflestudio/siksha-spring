package siksha.wafflestudio.core.service

import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFound
import siksha.wafflestudio.core.domain.restaurant.service.RestaurantService

@SpringBootTest
class RestaurantServiceTest
@Autowired
constructor(
    private val service: RestaurantService,
) {
    @Test
    fun testRestaurant() {
        val restaurants = service.getRestaurants()
        assert(restaurants.isNotEmpty())
    }

    @Test
    fun testRestaurantNotExists() {
        val testRestaurantId = -1L

        val exception = assertThrows<RestaurantNotFound> {
            service.getRestaurant(testRestaurantId)
        }

        assertEquals("Restaurant not found", exception.message)
    }
}
