package siksha.wafflestudio.core.repository.menu

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull
import siksha.wafflestudio.core.domain.main.menu.data.Menu
import siksha.wafflestudio.core.domain.main.menu.repository.MenuRepository
import siksha.wafflestudio.core.domain.main.restaurant.data.Restaurant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MenuTest {
    @Autowired
    private lateinit var menuRepository: MenuRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `save menu`() {
        // given
        val restaurant = entityManager.find(Restaurant::class.java, 1)
        assertNotNull(restaurant)

        val menu =
            Menu(
                id = 0,
                restaurant = restaurant,
                code = "TEST_MENU",
                date = LocalDate.now(),
                type = "LU",
                nameKr = "테스트 메뉴",
                nameEn = "Test Menu",
                price = 10000,
                etc = "[]",
            )

        // when
        val savedMenu = menuRepository.save(menu)
        entityManager.flush()
        entityManager.clear()

        // then
        assertNotNull(savedMenu)
        assertEquals(menu.nameKr, savedMenu.nameKr)
        assertEquals(menu.restaurant.id, savedMenu.restaurant.id)
    }

    @Test
    fun `get menu`() {
        // given
        val restaurant = entityManager.find(Restaurant::class.java, 1L)
        assertNotNull(restaurant)

        val menu =
            Menu(
                id = 0,
                restaurant = restaurant,
                code = "TEST_MENU",
                date = LocalDate.now(),
                type = "LU",
                nameKr = "테스트 메뉴",
                nameEn = "Test Menu",
                price = 10000,
                etc = "[]",
            )
        val savedMenu = entityManager.persist(menu)
        entityManager.flush()
        entityManager.clear()

        // when
        val foundMenu = menuRepository.findByIdOrNull(savedMenu.id)

        // then
        assertNotNull(foundMenu)
        assertEquals(savedMenu.nameKr, foundMenu.nameKr)
    }

    @Test
    fun `delete menu`() {
        // given
        val restaurant = entityManager.find(Restaurant::class.java, 1L)
        assertNotNull(restaurant)

        val menu =
            Menu(
                id = 0,
                restaurant = restaurant,
                code = "TEST_MENU",
                date = LocalDate.now(),
                type = "LU",
                nameKr = "테스트 메뉴",
                nameEn = "Test Menu",
                price = 10000,
                etc = "[]",
            )
        val savedMenu = entityManager.persist(menu)
        entityManager.flush()

        // when
        menuRepository.deleteById(savedMenu.id)
        entityManager.flush()
        entityManager.clear()
        val deletedMenu = menuRepository.findByIdOrNull(savedMenu.id)

        // then
        assertNull(deletedMenu)
    }
}
