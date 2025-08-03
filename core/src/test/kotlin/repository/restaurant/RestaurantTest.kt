package siksha.wafflestudio.core.repository.restaurant

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.jdbc.Sql
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantRepository

@DataJpaTest
@Sql("classpath:data/v001.sql")
class RestaurantRepositoryTest {

    @Autowired
    private lateinit var repository: RestaurantRepository

    @Test
    fun `findAll should return all restaurants`() {
        // when
        val restaurants = repository.findAll()

        // then
        assertThat(restaurants).hasSize(5)
    }

    @Test
    fun `findById should return a specific restaurant`() {
        // when
        val restaurant = repository.findById(1).orElse(null)

        // then
        assertThat(restaurant).isNotNull
        assertThat(restaurant!!.id).isEqualTo(1)
        assertThat(restaurant.code).isEqualTo("302동식당")
        assertThat(restaurant.nameKr).isEqualTo("302동식당")
    }
}
