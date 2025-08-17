package siksha.wafflestudio.core.repository.restaurant

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import siksha.wafflestudio.core.domain.main.restaurant.data.Restaurant
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantRepository
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RestaurantTest {
    @Autowired
    lateinit var repository: RestaurantRepository

    @Test
    fun `get restaurants`() {
        // given
        val savedRestaurant =
            repository.save(
                Restaurant(
                    code = "test",
                    nameKr = "test",
                    nameEn = "test",
                    addr = "test",
                    lat = 0.0,
                    lng = 0.0,
                    etc = null,
                ),
            )

        // when
        val retrievedRestaurant = repository.findByIdOrNull(savedRestaurant.id)

        // then
        assertNotNull(retrievedRestaurant)
        assertEquals(retrievedRestaurant.code, retrievedRestaurant.code)
    }
}
