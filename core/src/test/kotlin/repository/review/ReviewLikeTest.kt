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
import siksha.wafflestudio.core.domain.main.review.data.Review
import siksha.wafflestudio.core.domain.main.review.data.ReviewLike
import siksha.wafflestudio.core.domain.main.review.repository.ReviewLikeRepository
import siksha.wafflestudio.core.domain.user.data.User
import java.time.LocalDate
import java.time.OffsetDateTime

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewLikeTest {
    @Autowired
    private lateinit var reviewLikeRepository: ReviewLikeRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `save review like`() {
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

        val user2 =
            entityManager.persist(
                User(
                    id = 0,
                    type = "KAKAO",
                    identity = "00000002",
                    etc = null,
                    createdAt = OffsetDateTime.now(),
                    updatedAt = OffsetDateTime.now(),
                    nickname = "자반고등어구이",
                    profileUrl = null,
                )
            )

        val reviewLike = ReviewLike(
            id = 0,
            user = user2,
            review = review,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
        )

        // when
        val savedReviewLike = reviewLikeRepository.save(reviewLike)

        // then
        assertNotNull(savedReviewLike)
        assertEquals(review.id, savedReviewLike.review.id)
        assertEquals(user2.id, savedReviewLike.user.id)
    }

    @Test
    fun `delete review like`() {
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
                    type = "LU",
                    nameKr = "삭제 테스트 메뉴",
                    nameEn = "Delete Test Menu",
                    price = 15000,
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

        val user2 =
            entityManager.persist(
                User(
                    id = 0,
                    type = "GOOGLE",
                    identity = "00000003",
                    etc = null,
                    createdAt = OffsetDateTime.now(),
                    updatedAt = OffsetDateTime.now(),
                    nickname = "삭제테스트유저",
                    profileUrl = null,
                )
            )

        val reviewLike = ReviewLike(
            id = 0,
            user = user2,
            review = review,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
        )

        val savedReviewLike = reviewLikeRepository.save(reviewLike)
        entityManager.flush()
        entityManager.clear()

        // when
        reviewLikeRepository.delete(savedReviewLike)
        entityManager.flush()
        entityManager.clear()

        // then
        val deletedReviewLike = reviewLikeRepository.findById(savedReviewLike.id)
        assertNull(deletedReviewLike.orElse(null))
    }
}
