package siksha.wafflestudio.core.service.review

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import siksha.wafflestudio.core.domain.common.exception.CommentNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidScoreException
import siksha.wafflestudio.core.domain.common.exception.KeywordMissingException
import siksha.wafflestudio.core.domain.common.exception.MenuNotFoundException
import siksha.wafflestudio.core.domain.common.exception.ReviewAlreadyExistsException
import siksha.wafflestudio.core.domain.common.exception.ReviewNotFoundException
import siksha.wafflestudio.core.domain.common.exception.SelfReviewLikeNotAllowedException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.main.menu.data.Menu
import siksha.wafflestudio.core.domain.main.menu.dto.MenuLikeSummary
import siksha.wafflestudio.core.domain.main.menu.dto.MenuPlainSummary
import siksha.wafflestudio.core.domain.main.menu.dto.MenuSummary
import siksha.wafflestudio.core.domain.main.menu.repository.MenuRepository
import siksha.wafflestudio.core.domain.main.restaurant.data.Restaurant
import siksha.wafflestudio.core.domain.main.review.data.KeywordReview
import siksha.wafflestudio.core.domain.main.review.data.Review
import siksha.wafflestudio.core.domain.main.review.dto.ReviewRequest
import siksha.wafflestudio.core.domain.main.review.dto.ReviewSummary
import siksha.wafflestudio.core.domain.main.review.repository.KeywordReviewRepository
import siksha.wafflestudio.core.domain.main.review.repository.ReviewLikeRepository
import siksha.wafflestudio.core.domain.main.review.repository.ReviewRepository
import siksha.wafflestudio.core.domain.main.review.service.ReviewService
import siksha.wafflestudio.core.domain.user.data.User
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.s3.S3Service
import java.sql.Timestamp
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ReviewServiceTest {
    @Mock
    private lateinit var reviewRepository: ReviewRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var menuRepository: MenuRepository

    @Mock
    private lateinit var keywordReviewRepository: KeywordReviewRepository

    @Mock
    private lateinit var reviewLikeRepository: ReviewLikeRepository

    @Mock
    private lateinit var imageRepository: ImageRepository

    @Mock
    private lateinit var s3Service: S3Service

    private lateinit var objectMapper: ObjectMapper
    private lateinit var reviewService: ReviewService

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        reviewService =
            ReviewService(
                reviewRepository = reviewRepository,
                userRepository = userRepository,
                menuRepository = menuRepository,
                imageRepository = imageRepository,
                keywordReviewRepository = keywordReviewRepository,
                reviewLikeRepository = reviewLikeRepository,
                s3Service = s3Service,
            )
        // objectMapper는 private이므로 리플렉션을 사용하거나 다른 방법으로 설정
        val field = ReviewService::class.java.getDeclaredField("objectMapper")
        field.isAccessible = true
        field.set(reviewService, objectMapper)
    }

    @Test
    fun `postReview - 성공적으로 리뷰 작성`() {
        // given
        val userId = 1
        val menuId = 1
        val request =
            ReviewRequest(
                menuId = menuId,
                score = 5,
                comment = "생각보다 맛있어요",
                taste = "맛있음",
                price = "혜자스러워요",
                foodComposition = "알찬 편이에요",
            )

        val user =
            User(
                id = userId,
                type = "KAKAO",
                identity = "test123",
                etc = null,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                nickname = "테스트유저",
                profileUrl = null,
            )

        val restaurant =
            Restaurant(
                id = 1,
                code = "REST001",
                nameKr = "테스트식당",
                nameEn = "Test Restaurant",
                addr = "서울시",
                lat = 37.5,
                lng = 127.0,
                etc = null,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        val menu =
            Menu(
                id = menuId,
                restaurant = restaurant,
                code = "MENU001",
                date = LocalDate.now(),
                type = "LU",
                nameKr = "테스트메뉴",
                nameEn = "Test Menu",
                price = 10000,
                etc = "[]",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        val savedReview =
            Review(
                id = 1,
                user = user,
                menu = menu,
                score = 5,
                comment = "맛있어요!",
                etc = "",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        val menuSummary = mock<MenuSummary>()
        `when`(menuSummary.getId()).thenReturn(menuId)
        `when`(menuSummary.getRestaurantId()).thenReturn(1)
        `when`(menuSummary.getCode()).thenReturn("MENU001")
        `when`(menuSummary.getDate()).thenReturn(LocalDate.now())
        `when`(menuSummary.getType()).thenReturn("LU")
        `when`(menuSummary.getNameKr()).thenReturn("테스트메뉴")
        `when`(menuSummary.getNameEn()).thenReturn("Test Menu")
        `when`(menuSummary.getPrice()).thenReturn(10000)
        `when`(menuSummary.getEtc()).thenReturn("[]")
        `when`(menuSummary.getCreatedAt()).thenReturn(Timestamp(System.currentTimeMillis()))
        `when`(menuSummary.getUpdatedAt()).thenReturn(Timestamp(System.currentTimeMillis()))
        `when`(menuSummary.getScore()).thenReturn(4.5)
        `when`(menuSummary.getReviewCnt()).thenReturn(10)

        val menuLikeSummary = mock<MenuLikeSummary>()
        `when`(menuLikeSummary.getIsLiked()).thenReturn(0)
        `when`(menuLikeSummary.getLikeCnt()).thenReturn(0)

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(menuRepository.findById(menuId)).thenReturn(Optional.of(menu))
        `when`(reviewRepository.save(any())).thenReturn(savedReview)
        `when`(menuRepository.findMenuById(menuId.toString())).thenReturn(menuSummary)
        `when`(menuRepository.findMenuLikeByMenuIdAndUserId(menuId.toString(), userId.toString())).thenReturn(menuLikeSummary)

        // when
        val result = reviewService.postReview(userId, request)

        // then
        assertNotNull(result)
        verify(reviewRepository, times(1)).save(any())
        verify(keywordReviewRepository, times(1)).save(any())
    }

    @Test
    fun `postReview - 사용자를 찾을 수 없는 경우 예외 발생`() {
        // given
        val userId = 999
        val request =
            ReviewRequest(
                menuId = 1,
                score = 5,
                comment = "맛있어요!",
                taste = "맛있음",
                price = "적당함",
                foodComposition = "균형잡힘",
            )

        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())

        // when & then
        assertThrows(UserNotFoundException::class.java) {
            reviewService.postReview(userId, request)
        }
    }

    @Test
    fun `postReview - 메뉴를 찾을 수 없는 경우 예외 발생`() {
        // given
        val userId = 1
        val menuId = 999
        val request =
            ReviewRequest(
                menuId = menuId,
                score = 5,
                comment = "맛있어요!",
                taste = "맛있음",
                price = "적당함",
                foodComposition = "균형잡힘",
            )

        val user =
            User(
                id = userId,
                type = "KAKAO",
                identity = "test123",
                etc = null,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                nickname = "테스트유저",
                profileUrl = null,
            )

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(menuRepository.findById(menuId)).thenReturn(Optional.empty())

        // when & then
        assertThrows(MenuNotFoundException::class.java) {
            reviewService.postReview(userId, request)
        }
    }

    @Test
    fun `postReview - 키워드가 부족한 경우 예외 발생`() {
        // given
        val userId = 1
        val request =
            ReviewRequest(
                menuId = 1,
                score = 5,
                comment = "맛있어요!",
                taste = "맛있음",
                price = "",
                foodComposition = "",
            )

        // when & then
        assertThrows(KeywordMissingException::class.java) {
            reviewService.postReview(userId, request)
        }
    }

    @Test
    fun `postReview - 중복 리뷰 작성 시 예외 발생`() {
        // given
        val userId = 1
        val menuId = 1
        val request =
            ReviewRequest(
                menuId = menuId,
                score = 5,
                comment = "맛있어요!",
                taste = "맛있음",
                price = "적당함",
                foodComposition = "균형잡힘",
            )

        val user =
            User(
                id = userId,
                type = "KAKAO",
                identity = "test123",
                etc = null,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                nickname = "테스트유저",
                profileUrl = null,
            )

        val restaurant =
            Restaurant(
                id = 1,
                code = "REST001",
                nameKr = "테스트식당",
                nameEn = "Test Restaurant",
                addr = "서울시",
                lat = 37.5,
                lng = 127.0,
                etc = null,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        val menu =
            Menu(
                id = menuId,
                restaurant = restaurant,
                code = "MENU001",
                date = LocalDate.now(),
                type = "LU",
                nameKr = "테스트메뉴",
                nameEn = "Test Menu",
                price = 10000,
                etc = "[]",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(menuRepository.findById(menuId)).thenReturn(Optional.of(menu))
        whenever(reviewRepository.save(any<Review>()))
            .thenThrow(DataIntegrityViolationException("Duplicate entry"))
        assertThrows(ReviewAlreadyExistsException::class.java) {
            reviewService.postReview(userId, request)
        }
        verify(reviewRepository, times(1)).save(any())
        verify(keywordReviewRepository, never()).save(any())
    }

    @Test
    fun `likeReview - 성공적으로 좋아요 추가`() {
        // given
        val reviewId = 1
        val userId = 1

        val user =
            User(
                id = userId,
                type = "KAKAO",
                identity = "test123",
                etc = null,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                nickname = "테스트유저",
                profileUrl = null,
            )

        val otherUser =
            User(
                id = 2,
                type = "GOOGLE",
                identity = "test456",
                etc = null,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                nickname = "다른유저",
                profileUrl = null,
            )

        val restaurant =
            Restaurant(
                id = 1,
                code = "REST001",
                nameKr = "테스트식당",
                nameEn = "Test Restaurant",
                addr = "서울시",
                lat = 37.5,
                lng = 127.0,
                etc = null,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        val menu =
            Menu(
                id = 1,
                restaurant = restaurant,
                code = "MENU001",
                date = LocalDate.now(),
                type = "LU",
                nameKr = "테스트메뉴",
                nameEn = "Test Menu",
                price = 10000,
                etc = "[]",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        val review =
            Review(
                id = reviewId,
                user = otherUser,
                menu = menu,
                score = 5,
                comment = "맛있어요!",
                etc = "",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        `when`(userRepository.getReferenceById(userId)).thenReturn(user)
        `when`(reviewRepository.getReferenceById(reviewId)).thenReturn(review)

        // when
        reviewService.likeReview(reviewId, userId)

        // then
        verify(reviewLikeRepository, times(1)).save(any())
    }

    @Test
    fun `likeReview - 자신의 리뷰에 좋아요 시 예외 발생`() {
        // given
        val reviewId = 1
        val userId = 1

        val user =
            User(
                id = userId,
                type = "KAKAO",
                identity = "test123",
                etc = null,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                nickname = "테스트유저",
                profileUrl = null,
            )

        val restaurant =
            Restaurant(
                id = 1,
                code = "REST001",
                nameKr = "테스트식당",
                nameEn = "Test Restaurant",
                addr = "서울시",
                lat = 37.5,
                lng = 127.0,
                etc = null,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        val menu =
            Menu(
                id = 1,
                restaurant = restaurant,
                code = "MENU001",
                date = LocalDate.now(),
                type = "LU",
                nameKr = "테스트메뉴",
                nameEn = "Test Menu",
                price = 10000,
                etc = "[]",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        val review =
            Review(
                id = reviewId,
                user = user,
                menu = menu,
                score = 5,
                comment = "맛있어요!",
                etc = "",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        `when`(userRepository.getReferenceById(userId)).thenReturn(user)
        `when`(reviewRepository.getReferenceById(reviewId)).thenReturn(review)

        // when & then
        assertThrows(SelfReviewLikeNotAllowedException::class.java) {
            reviewService.likeReview(reviewId, userId)
        }

        verify(reviewLikeRepository, never()).save(any())
    }

    @Test
    fun `unlikeReview - 성공적으로 좋아요 취소`() {
        // given
        val reviewId = 1
        val userId = 1

        `when`(reviewLikeRepository.deleteByUserIdAndReviewId(userId, reviewId)).thenReturn(1)

        // when
        val result = reviewService.unlikeReview(reviewId, userId)

        // then
        assertEquals(true, result)
        verify(reviewLikeRepository).deleteByUserIdAndReviewId(userId, reviewId)
    }

    @Test
    fun `unlikeReview - 좋아요가 없는 경우 false 반환`() {
        // given
        val reviewId = 1
        val userId = 1

        `when`(reviewLikeRepository.deleteByUserIdAndReviewId(userId, reviewId)).thenReturn(0)

        // when
        val result = reviewService.unlikeReview(reviewId, userId)

        // then
        assertEquals(false, result)
        verify(reviewLikeRepository).deleteByUserIdAndReviewId(userId, reviewId)
    }

    @Test
    fun `getCommentRecommendation - 유효한 점수로 댓글 추천 성공`() {
        // given
        val score = 5
        val expectedComment = "정말 맛있었어요!"

        `when`(reviewRepository.findRandomCommentByScore(score)).thenReturn(expectedComment)

        // when
        val result = reviewService.getCommentRecommendation(score)

        // then
        assertNotNull(result)
        assertEquals(expectedComment, result.comment)
    }

    @Test
    fun `getCommentRecommendation - 잘못된 점수로 예외 발생`() {
        // given
        val invalidScore = 6

        // when & then
        assertThrows(InvalidScoreException::class.java) {
            reviewService.getCommentRecommendation(invalidScore)
        }
    }

    @Test
    fun `getCommentRecommendation - 댓글을 찾을 수 없는 경우 예외 발생`() {
        // given
        val score = 3

        `when`(reviewRepository.findRandomCommentByScore(score)).thenReturn(null)

        // when & then
        assertThrows(CommentNotFoundException::class.java) {
            reviewService.getCommentRecommendation(score)
        }
    }

    @Test
    fun `getScoreDistribution - 점수 분포 조회 성공`() {
        // given
        val menuId = 1
        val menuPlainSummary = mock<MenuPlainSummary>()
        `when`(menuPlainSummary.getRestaurantId()).thenReturn(1)
        `when`(menuPlainSummary.getCode()).thenReturn("MENU001")

        val scoreCounts =
            listOf(
                arrayOf<Any>(5, 10),
                arrayOf<Any>(4, 5),
                arrayOf<Any>(3, 2),
                arrayOf<Any>(2, 1),
                arrayOf<Any>(1, 0),
            )

        `when`(menuRepository.findPlainMenuById(menuId.toString())).thenReturn(menuPlainSummary)
        `when`(reviewRepository.findScoreCountsByMenuId(1, "MENU001")).thenReturn(scoreCounts)

        // when
        val result = reviewService.getScoreDistribution(menuId)

        // then
        assertNotNull(result)
        assertEquals(listOf(0, 1, 2, 5, 10), result.dist)

        val dist = result.dist
        assertEquals(10, dist[4]) // score 5
        assertEquals(5, dist[3]) // score 4
        assertEquals(0, dist[0]) // score 1
    }

    @Test
    fun `getMyReviews - 내 리뷰 목록 조회 성공`() {
        // given
        val userId = 1
        val page = 1
        val size = 10

        // 레스토랑 2곳에 대한 리뷰가 있다고 가정
        val rest1 =
            Restaurant(
                id = 100,
                code = "R100",
                nameKr = "식당1",
                nameEn = "R1",
                addr = "서울",
                lat = 0.0, lng = 0.0, etc = null,
                createdAt = OffsetDateTime.now(), updatedAt = OffsetDateTime.now(),
            )
        val rest2 =
            Restaurant(
                id = 200,
                code = "R200",
                nameKr = "식당2",
                nameEn = "R2",
                addr = "서울",
                lat = 0.0, lng = 0.0, etc = null,
                createdAt = OffsetDateTime.now(), updatedAt = OffsetDateTime.now(),
            )

        val menu1 =
            Menu(
                id = 11,
                restaurant = rest1,
                code = "M11",
                date = LocalDate.now(),
                type = "LU",
                nameKr = "메뉴1",
                nameEn = "Menu1",
                price = 5000,
                etc = "[]",
                createdAt = OffsetDateTime.now(), updatedAt = OffsetDateTime.now(),
            )
        val menu2 =
            Menu(
                id = 22,
                restaurant = rest2,
                code = "M22",
                date = LocalDate.now(),
                type = "LU",
                nameKr = "메뉴2",
                nameEn = "Menu2",
                price = 6000,
                etc = "[]",
                createdAt = OffsetDateTime.now(), updatedAt = OffsetDateTime.now(),
            )

        val r1 =
            Review(
                id = 1,
                user =
                    User(
                        id = userId,
                        type = "KAKAO",
                        identity = "u",
                        etc = null,
                        createdAt = OffsetDateTime.now(),
                        updatedAt = OffsetDateTime.now(),
                        nickname = "u",
                        profileUrl = null,
                    ),
                menu = menu1,
                score = 5,
                comment = "굿",
                etc = "",
                createdAt = OffsetDateTime.now().minusDays(1),
                updatedAt = OffsetDateTime.now().minusDays(1),
            )
        val r2 =
            Review(
                id = 2,
                user = r1.user,
                menu = menu2,
                score = 3,
                comment = "쏘쏘",
                etc = "",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        val kr1 =
            KeywordReview(
                id = r1.id,
                taste = 5,
                price = 4,
                foodComposition = 3,
                review = r1,
                menu = menu1,
            )
        val kr2 =
            KeywordReview(
                id = r2.id,
                taste = 3,
                price = 2,
                foodComposition = 1,
                review = r2,
                menu = menu2,
            )

        whenever(keywordReviewRepository.findAllById(any())).thenReturn(listOf(kr1, kr2))
        whenever(reviewRepository.countDistinctRestaurantsByUserId(userId)).thenReturn(2L)
        whenever(reviewRepository.findRestaurantIdsByUserIdPaged(eq(userId), any())).thenReturn(listOf(100, 200))
        whenever(reviewRepository.findAllByUserIdAndRestaurantIds(eq(userId), any())).thenReturn(listOf(r1, r2))

        // when
        val result = reviewService.getMyReviews(userId, page, size)

        // then
        // 1) 널 체크 먼저
        assertNotNull(result)

        // 2) 크기 체크 후 인덱싱
        assertEquals(2, result.result.size) // 식당 그룹 2개
        assertEquals(1, result.result[0].reviews.size)
        assertEquals(1, result.result[1].reviews.size)

        // 3) 값 검증
        assertEquals(2, result.totalCount) // 서로 다른 레스토랑 수
        assertEquals(false, result.hasNext)
        assertEquals(100, result.result[0].restaurantId) // 첫 그룹이 식당1(100), 두 번째가 식당2(200)인지 확인
        assertEquals(200, result.result[1].restaurantId)
        assertEquals(3, result.result[0].reviews[0].keywordReviews.size)
        assertEquals(3, result.result[1].reviews[0].keywordReviews.size)

        // 4) 호출 검증
        val pageCaptor = argumentCaptor<PageRequest>()
        verify(reviewRepository).findRestaurantIdsByUserIdPaged(eq(userId), pageCaptor.capture())
        assertEquals(size, pageCaptor.firstValue.pageSize)
        assertEquals(page - 1, pageCaptor.firstValue.pageNumber)
        verify(keywordReviewRepository).findAllById(listOf(r1.id, r2.id))
    }

    @Test
    fun `getReview - 리뷰가 없는 경우 예외`() {
        // given
        val reviewId = 999
        whenever(reviewRepository.findById(reviewId)).thenReturn(Optional.empty())

        // when & then
        assertThrows(ReviewNotFoundException::class.java) {
            reviewService.getReview(reviewId, 0)
        }
        verify(reviewRepository).findById(reviewId)
        // 키워드 조회는 호출되지 않아야 함
        verify(keywordReviewRepository, never()).findById(any())
    }

    @Test
    fun `getReview - 키워드가 없는 경우 null 리턴`() {
        // given
        val reviewId = 10
        val user =
            User(
                id = 1,
                type = "KAKAO",
                identity = "u",
                etc = null,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                nickname = "u",
                profileUrl = null,
            )
        val restaurant =
            Restaurant(
                id = 100, code = "R100", nameKr = "식당", nameEn = "R",
                addr = "서울", lat = .0, lng = .0, etc = null,
                createdAt = OffsetDateTime.now(), updatedAt = OffsetDateTime.now(),
            )
        val menu =
            Menu(
                id = 11, restaurant = restaurant, code = "M11",
                date = LocalDate.now(), type = "LU",
                nameKr = "메뉴", nameEn = "Menu", price = 5000, etc = "[]",
                createdAt = OffsetDateTime.now(), updatedAt = OffsetDateTime.now(),
            )
        val review =
            Review(
                id = reviewId,
                user = user,
                menu = menu,
                score = 5,
                comment = "굿",
                etc = "",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        whenever(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review))
        whenever(keywordReviewRepository.findById(reviewId)).thenReturn(Optional.empty())

        // when
        val result = reviewService.getReview(reviewId, 0)

        // then
        assertNotNull(result)
        assertEquals(listOf(null, null, null), result.keywordReviews)
        verify(reviewRepository).findById(reviewId)
        verify(keywordReviewRepository).findById(reviewId)
    }

    @Test
    fun `getFilteredReviews - 필터링된 리뷰 조회 성공`() {
        // given
        val userId = 1
        val menuId = 1
        val comment = true
        val etc = false
        val page = 1
        val size = 10

        val menuPlainSummary = mock<MenuPlainSummary>()
        `when`(menuPlainSummary.getRestaurantId()).thenReturn(1)
        `when`(menuPlainSummary.getCode()).thenReturn("MENU001")

        val reviewSummary = mock<ReviewSummary>()
        `when`(reviewSummary.getId()).thenReturn(1)
        `when`(reviewSummary.getMenuId()).thenReturn(1)
        `when`(reviewSummary.getUserId()).thenReturn(userId)
        `when`(reviewSummary.getScore()).thenReturn(5)
        `when`(reviewSummary.getComment()).thenReturn("맛있어요!")
        `when`(reviewSummary.getEtc()).thenReturn("")
        `when`(reviewSummary.getTaste()).thenReturn(5)
        `when`(reviewSummary.getPrice()).thenReturn(4)
        `when`(reviewSummary.getFoodComposition()).thenReturn(4)
        `when`(reviewSummary.getLikeCount()).thenReturn(3)
        `when`(reviewSummary.getIsLiked()).thenReturn(0)
        `when`(reviewSummary.getCreatedAt()).thenReturn(Timestamp.valueOf(OffsetDateTime.now().toLocalDateTime()))
        `when`(reviewSummary.getUpdatedAt()).thenReturn(Timestamp.valueOf(OffsetDateTime.now().toLocalDateTime()))

        val reviews = listOf(reviewSummary)

        `when`(menuRepository.findPlainMenuById(menuId.toString())).thenReturn(menuPlainSummary)
        whenever(
            reviewRepository.findFilteredReviews(eq(userId), eq(1), eq("MENU001"), eq(comment), eq(etc), any()),
        ).thenReturn(reviews)
        `when`(reviewRepository.countFilteredReviews(1, "MENU001", comment, etc)).thenReturn(1L)

        // when
        val result = reviewService.getFilteredReviews(userId, menuId, comment, etc, page, size)

        // then
        assertNotNull(result)
        assertEquals(1, result.totalCount)
        assertEquals(false, result.hasNext)
        assertEquals(1, result.result.size)

        val pageableCaptor = argumentCaptor<PageRequest>()
        verify(reviewRepository).findFilteredReviews(eq(userId), eq(1), eq("MENU001"), eq(comment), eq(etc), pageableCaptor.capture())
        val order = pageableCaptor.firstValue.sort.getOrderFor("created_at")
        assertEquals(Sort.Direction.DESC, order?.direction)
        assertEquals(0, pageableCaptor.firstValue.pageNumber)
        assertEquals(10, pageableCaptor.firstValue.pageSize)

        verify(reviewRepository, times(1)).countFilteredReviews(1, "MENU001", comment, etc)
    }
}
