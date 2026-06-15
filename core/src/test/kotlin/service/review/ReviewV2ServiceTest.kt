package service.review

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.main.menu.data.MenuV2
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2DetailsDto
import siksha.wafflestudio.core.domain.main.menu.repository.MenuV2Repository
import siksha.wafflestudio.core.domain.main.menu.service.MenuV2Service
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.review.data.KeywordReviewV2
import siksha.wafflestudio.core.domain.main.review.data.ReviewLikeV2
import siksha.wafflestudio.core.domain.main.review.data.ReviewV2
import siksha.wafflestudio.core.domain.main.review.dto.ReviewV2Request
import siksha.wafflestudio.core.domain.main.review.dto.ReviewV2Summary
import siksha.wafflestudio.core.domain.main.review.repository.KeywordReviewV2Repository
import siksha.wafflestudio.core.domain.main.review.repository.ReviewLikeV2Repository
import siksha.wafflestudio.core.domain.main.review.repository.ReviewV2Repository
import siksha.wafflestudio.core.domain.main.review.service.ReviewV2Service
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.imageupload.ImageUploadUseCase
import java.sql.Timestamp
import java.time.Instant
import java.util.Optional

class ReviewV2ServiceTest {
    private lateinit var reviewRepository: ReviewV2Repository
    private lateinit var userRepository: UserRepository
    private lateinit var menuRepository: MenuV2Repository
    private lateinit var menuService: MenuV2Service
    private lateinit var imageRepository: ImageRepository
    private lateinit var keywordReviewRepository: KeywordReviewV2Repository
    private lateinit var reviewLikeRepository: ReviewLikeV2Repository
    private lateinit var imageUploadUseCase: ImageUploadUseCase
    private lateinit var service: ReviewV2Service

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        reviewRepository = mockk()
        userRepository = mockk()
        menuRepository = mockk()
        menuService = mockk()
        imageRepository = mockk()
        keywordReviewRepository = mockk()
        reviewLikeRepository = mockk()
        imageUploadUseCase = mockk()
        service =
            ReviewV2Service(
                reviewRepository,
                userRepository,
                menuRepository,
                menuService,
                imageRepository,
                keywordReviewRepository,
                reviewLikeRepository,
                imageUploadUseCase,
            )
    }

    @Test
    fun `post review writes review v2 and keyword review v2`() {
        val user = testUser(1)
        val menu = testMenu()
        val keywordSlot = slot<KeywordReviewV2>()

        every { userRepository.findById(1) } returns Optional.of(user)
        every { menuRepository.findById(10) } returns Optional.of(menu)
        every { reviewRepository.existsByMenuIdAndUserId(10, 1) } returns false
        every { reviewRepository.save(any()) } answers {
            val review = firstArg<ReviewV2>()
            ReviewV2(id = 100, menu = review.menu, user = review.user, score = review.score, comment = review.comment, etc = review.etc)
        }
        every { keywordReviewRepository.save(capture(keywordSlot)) } answers { firstArg() }
        every { menuService.getMenuById(menuId = 10, userId = 1) } returns menuDetails()

        val result =
            service.postReview(
                userId = 1,
                request = ReviewV2Request(menu_id = 10, score = 5, comment = "good"),
            )

        assertEquals(10, result.menuId)
        assertEquals(-1, keywordSlot.captured.taste)
        assertEquals(-1, keywordSlot.captured.price)
        assertEquals(-1, keywordSlot.captured.foodComposition)
        verify { reviewRepository.save(any()) }
        verify { keywordReviewRepository.save(any()) }
    }

    @Test
    fun `get reviews reads review v2 by normalized menu id`() {
        every { menuRepository.existsById(10) } returns true
        every { reviewRepository.findByMenuIdOrderByCreatedAtDesc(1, 10, any()) } returns
            listOf(reviewSummary(reviewId = 100, menuId = 10, isLiked = 1))
        every { reviewRepository.countByMenuId(10) } returns 1

        val result = service.getReviews(userId = 1, menuId = 10, page = 1, size = 10)

        assertEquals(1, result.totalCount)
        assertFalse(result.hasNext)
        assertEquals(100, result.result[0].id)
        assertEquals(10, result.result[0].menuId)
        assertTrue(result.result[0].isLiked)
    }

    @Test
    fun `like review writes review like v2 when review is not mine`() {
        val user = testUser(1)
        val reviewOwner = testUser(2)
        val review = ReviewV2(id = 100, menu = testMenu(), user = reviewOwner, score = 5, comment = "good")

        every { userRepository.findById(1) } returns Optional.of(user)
        every { reviewRepository.findByIdForDetail(100) } returns review
        every { reviewLikeRepository.existsByUserIdAndReviewId(1, 100) } returns false
        every { reviewLikeRepository.save(any()) } answers { firstArg<ReviewLikeV2>() }

        service.likeReview(reviewId = 100, userId = 1)

        verify { reviewLikeRepository.save(any()) }
    }

    private fun testMenu(): MenuV2 {
        val building = BuildingV2(id = 1, number = "301", name = "Building", defaultOrder = 1)
        val restaurant = RestaurantV2(id = 1, building = building, name = "Restaurant", defaultOrder = 1)
        return MenuV2(id = 10, restaurant = restaurant, name = "Menu")
    }

    private fun testUser(id: Int): User =
        User(
            id = id,
            type = "TEST",
            identity = "user-$id",
            nickname = "user$id",
        )

    private fun menuDetails(): MenuV2DetailsDto =
        MenuV2DetailsDto(
            menuId = 10,
            menuName = "Menu",
            restaurantId = 1,
            restaurantName = "Restaurant",
            buildingNumber = "301",
            buildingName = "Building",
            score = null,
            reviewCnt = 1,
            likeCnt = 0,
            isLiked = false,
            meals = emptyList(),
        )

    private fun reviewSummary(
        reviewId: Long,
        menuId: Long,
        isLiked: Int,
    ): ReviewV2Summary = TestReviewSummary(reviewId = reviewId, menuId = menuId, isLiked = isLiked)

    private data class TestReviewSummary(
        private val reviewId: Long,
        private val menuId: Long,
        private val isLiked: Int,
    ) : ReviewV2Summary {
        override fun getId(): Long = reviewId

        override fun getMenuId(): Long = menuId

        override fun getMenuName(): String = "Menu"

        override fun getRestaurantId(): Int = 1

        override fun getRestaurantName(): String = "Restaurant"

        override fun getUserId(): Int = 1

        override fun getScore(): Int = 5

        override fun getComment(): String = "good"

        override fun getEtc(): String? = null

        override fun getTaste(): Int? = null

        override fun getPrice(): Int? = null

        override fun getFoodComposition(): Int? = null

        override fun getLikeCount(): Int = 1

        override fun getIsLiked(): Int = isLiked

        override fun getCreatedAt(): Timestamp = timestamp()

        override fun getUpdatedAt(): Timestamp = timestamp()
    }

    companion object {
        private fun timestamp(): Timestamp = Timestamp.from(Instant.parse("2026-06-02T00:00:00Z"))
    }
}
