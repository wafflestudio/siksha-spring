package siksha.wafflestudio.core.repository.review

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import siksha.wafflestudio.core.domain.main.menu.data.Menu
import siksha.wafflestudio.core.domain.main.restaurant.data.Restaurant
import siksha.wafflestudio.core.domain.main.review.data.KeywordReview
import siksha.wafflestudio.core.domain.main.review.data.Review
import siksha.wafflestudio.core.domain.main.review.repository.KeywordReviewRepository
import siksha.wafflestudio.core.domain.user.data.User
import java.time.LocalDate

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KeywordReviewTest {
    @Autowired
    private lateinit var keywordReviewRepository: KeywordReviewRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `save keyword review`() {
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

        val keywordReview =
            KeywordReview(
                review = review,
                menu = menu,
                taste = 4,
                price = 3,
                foodComposition = 3,
            )

        // when
        val savedKeywordReview = keywordReviewRepository.save(keywordReview)
        entityManager.flush()
        entityManager.clear()

        // then
        assertNotNull(savedKeywordReview)
        assertEquals(review.id, keywordReview.review.id)
        assertEquals(menu.restaurant.id, keywordReview.menu.restaurant.id)
        assertEquals(4, keywordReview.taste)
        assertEquals(3, keywordReview.price)
        assertEquals(3, keywordReview.foodComposition)
    }

    @Test
    fun `update keyword review`() {
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
                    code = "TEST_MENU_UPDATE",
                    date = LocalDate.now(),
                    type = "DN",
                    nameKr = "업데이트 테스트 메뉴",
                    nameEn = "Update Test Menu",
                    price = 12000,
                    etc = "[]",
                ),
            )

        val review =
            entityManager.persist(
                Review(
                    id = 0,
                    user = user,
                    menu = menu,
                    score = 3,
                    comment = "업데이트 테스트용 리뷰",
                    etc = null,
                ),
            )

        val keywordReview =
            KeywordReview(
                review = review,
                menu = menu,
                taste = 2,
                price = 2,
                foodComposition = 2,
            )

        val savedKeywordReview = keywordReviewRepository.save(keywordReview)
        entityManager.flush()
        entityManager.clear()

        // when
        val updatedKeywordReview =
            savedKeywordReview.copy(
                taste = 5,
                price = 4,
                foodComposition = 4,
            )
        val result = keywordReviewRepository.save(updatedKeywordReview)
        entityManager.flush()
        entityManager.clear()

        // then
        val foundKeywordReview = keywordReviewRepository.findById(result.review.id)
        assertNotNull(foundKeywordReview.orElse(null))
        assertEquals(5, foundKeywordReview.get().taste)
        assertEquals(4, foundKeywordReview.get().price)
        assertEquals(4, foundKeywordReview.get().foodComposition)
    }

    @Test
    fun `delete keyword review`() {
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
                    code = "TEST_MENU_DELETE",
                    date = LocalDate.now(),
                    type = "BR",
                    nameKr = "삭제 테스트 메뉴",
                    nameEn = "Delete Test Menu",
                    price = 8000,
                    etc = "[]",
                ),
            )

        val review =
            entityManager.persist(
                Review(
                    id = 0,
                    user = user,
                    menu = menu,
                    score = 4,
                    comment = "삭제 테스트용 리뷰",
                    etc = null,
                ),
            )

        val keywordReview =
            KeywordReview(
                review = review,
                menu = menu,
                taste = 3,
                price = 3,
                foodComposition = 3,
            )

        val savedKeywordReview = keywordReviewRepository.save(keywordReview)
        entityManager.flush()
        entityManager.clear()

        // when
        keywordReviewRepository.delete(savedKeywordReview)
        entityManager.flush()
        entityManager.clear()

        // then
        val deletedKeywordReview = keywordReviewRepository.findById(savedKeywordReview.review.id)
        assertNull(deletedKeywordReview.orElse(null))
    }
}
