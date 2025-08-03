package siksha.wafflestudio.core.repository.review

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull
import siksha.wafflestudio.core.domain.main.menu.data.Menu
import siksha.wafflestudio.core.domain.main.restaurant.data.Restaurant
import siksha.wafflestudio.core.domain.main.review.data.Review
import siksha.wafflestudio.core.domain.main.review.repository.ReviewRepository
import siksha.wafflestudio.core.domain.user.data.User
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewTest {
    @Autowired
    private lateinit var reviewRepository: ReviewRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `save review`() {
        // given
        val user = entityManager.find(User::class.java, 1L)
        assertNotNull(user)

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

        val review =
            Review(
                id = 0,
                user = user,
                menu = savedMenu,
                score = 5,
                comment = "맛있어요!",
                etc = null,
            )

        // when
        val savedReview = reviewRepository.save(review)
        entityManager.flush()
        entityManager.clear()

        // then
        assertNotNull(savedReview)
        assertEquals(review.comment, savedReview.comment)
        assertEquals(review.user.id, savedReview.user.id)
        assertEquals(review.menu.id, savedReview.menu.id)
    }

    @Test
    fun `get review`() {
        // given
        val user = entityManager.find(User::class.java, 1L)
        assertNotNull(user)

        val restaurant = entityManager.find(Restaurant::class.java, 1L)
        assertNotNull(restaurant)

        val menu =
            entityManager.persist(
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
                ),
            )

        val review =
            entityManager.persist(
                Review(
                    id = 0,
                    user = user,
                    menu = menu,
                    score = 5,
                    comment = "맛있어요!",
                    etc = null,
                ),
            )
        entityManager.flush()
        entityManager.clear()

        // when
        val foundReview = reviewRepository.findByIdOrNull(review.id)

        // then
        assertNotNull(foundReview)
        assertEquals(review.comment, foundReview.comment)
    }

    @Test
    fun `delete review`() {
        // given
        val user = entityManager.find(User::class.java, 1L)
        assertNotNull(user)

        val restaurant = entityManager.find(Restaurant::class.java, 1L)
        assertNotNull(restaurant)

        val menu =
            entityManager.persist(
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
                ),
            )

        val review =
            entityManager.persist(
                Review(
                    id = 0,
                    user = user,
                    menu = menu,
                    score = 5,
                    comment = "맛있어요!",
                    etc = null,
                ),
            )
        entityManager.flush()

        // when
        reviewRepository.deleteById(review.id)
        entityManager.flush()
        entityManager.clear()
        val deletedReview = reviewRepository.findByIdOrNull(review.id)

        // then
        assertNull(deletedReview)
    }
}
