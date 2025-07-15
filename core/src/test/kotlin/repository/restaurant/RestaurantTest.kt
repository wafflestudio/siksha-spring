package siksha.wafflestudio.core.repository.restaurant

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantRepository

@SpringBootTest
class CommentTest
@Autowired
constructor(
    private val repository: RestaurantRepository,
) {
    @Test
    fun testRestaurant() {
        val restaurants = repository.findAll()
        assert(restaurants.isNotEmpty())
    }
}
