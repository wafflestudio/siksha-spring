package siksha.wafflestudio.core.repository.restaurant

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import siksha.wafflestudio.core.domain.restaurant.data.RestaurantRepository

@SpringBootTest
class RestaurantTest
    @Autowired
    constructor(
        private val repository: RestaurantRepository,
    ) {
        @Test
        fun testRestaurant() {
            println(repository.findAll().map{ it.nameKr })
        }
    }
