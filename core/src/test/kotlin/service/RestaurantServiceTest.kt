package siksha.wafflestudio.core.service.restaurant

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
        val restaurants = service.getRestaurants(1)
        assert(restaurants.isNotEmpty())
    }

    @Test
    fun testRestaurantNotExists() {
        val testRestaurantId = -1

        val exception = assertThrows<RestaurantNotFound> {
            service.getRestaurant(testRestaurantId)
        }

        assertEquals("Restaurant not found", exception.message)
    }
}
