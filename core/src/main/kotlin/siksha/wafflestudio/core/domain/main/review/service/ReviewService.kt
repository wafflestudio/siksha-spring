package siksha.wafflestudio.core.domain.main.review.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import siksha.wafflestudio.core.domain.common.exception.CommentNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidScoreException
import siksha.wafflestudio.core.domain.common.exception.KeywordMissingException
import siksha.wafflestudio.core.domain.common.exception.MenuNotFoundException
import siksha.wafflestudio.core.domain.common.exception.ReviewAlreadyExistsException
import siksha.wafflestudio.core.domain.common.exception.ReviewSaveFailedException
import siksha.wafflestudio.core.domain.common.exception.SelfReviewLikeNotAllowedException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.image.data.Image
import siksha.wafflestudio.core.domain.image.data.ImageCategory
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.main.menu.dto.MenuDetailsDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuPlainSummary
import siksha.wafflestudio.core.domain.main.menu.repository.MenuRepository
import siksha.wafflestudio.core.domain.main.review.data.KeywordReview
import siksha.wafflestudio.core.domain.main.review.data.Review
import siksha.wafflestudio.core.domain.main.review.data.ReviewLike
import siksha.wafflestudio.core.domain.main.review.dto.CommentRecommendationResponse
import siksha.wafflestudio.core.domain.main.review.dto.KeywordReviewSummary
import siksha.wafflestudio.core.domain.main.review.dto.KeywordScoreDistributionResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewListResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewRequest
import siksha.wafflestudio.core.domain.main.review.dto.ReviewResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewScoreDistributionResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewWithImagesRequest
import siksha.wafflestudio.core.domain.main.review.repository.KeywordReviewRepository
import siksha.wafflestudio.core.domain.main.review.repository.ReviewLikeRepository
import siksha.wafflestudio.core.domain.main.review.repository.ReviewRepository
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.s3.S3ImagePrefix
import siksha.wafflestudio.core.infrastructure.s3.S3Service
import siksha.wafflestudio.core.util.KeywordReviewUtil
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val menuRepository: MenuRepository,
    private val imageRepository: ImageRepository,
    private val keywordReviewRepository: KeywordReviewRepository,
    private val reviewLikeRepository: ReviewLikeRepository,
    private val s3Service: S3Service,
) {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    fun postReviewWithImages(
        userId: Int,
        reviewWithImagesRequest: ReviewWithImagesRequest,
    ): MenuDetailsDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        val menuId = reviewWithImagesRequest.menuId
        val menu = menuRepository.findByIdOrNull(menuId) ?: throw MenuNotFoundException()
        val score = reviewWithImagesRequest.score
        val comment = reviewWithImagesRequest.comment
        val tasteKeyword = reviewWithImagesRequest.taste
        val priceKeyword = reviewWithImagesRequest.price
        val foodCompositionKeyword = reviewWithImagesRequest.foodComposition
        val images = reviewWithImagesRequest.images

        validatePartialEmptyFields(tasteKeyword, priceKeyword, foodCompositionKeyword)

        val uploadedFiles =
            images?.takeIf { it.isNotEmpty() }?.let {
                s3Service.uploadFiles(
                    it,
                    S3ImagePrefix.REVIEW,
                    "menu-$menuId/user-$userId/review-${OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))}",
                )
            } ?: emptyList()

        imageRepository.saveAll(
            uploadedFiles.map { dto ->
                Image(
                    key = dto.key,
                    category = ImageCategory.REVIEW,
                    userId = userId,
                    isDeleted = false,
                )
            },
        )

        val imageUrls = uploadedFiles.map { it.url }

        val review: Review
        try {
            review =
                reviewRepository.save(
                    Review(
                        user = user,
                        menu = menu,
                        score = score,
                        comment = comment ?: "",
                        etc = objectMapper.writeValueAsString(imageUrls),
                        // 이미지 URL 들을 JSON array로 저장
                    ),
                )
        } catch (ex: DataIntegrityViolationException) {
            throw ReviewAlreadyExistsException()
        } catch (ex: Exception) {
            throw ReviewSaveFailedException()
        }

        keywordReviewRepository.save(
            KeywordReview(
                taste = KeywordReviewUtil.getTasteLevel(tasteKeyword),
                price = KeywordReviewUtil.getPriceLevel(priceKeyword),
                foodComposition = KeywordReviewUtil.getFoodCompositionLevel(foodCompositionKeyword),
                review = review,
                menu = menu,
            ),
        )

        val menuSummary = menuRepository.findMenuById(menu.id.toString())
        val menuLikeSummary =
            menuRepository.findMenuLikeByMenuIdAndUserId(
                menu.id.toString(),
                user.id.toString(),
            )

        return MenuDetailsDto.from(menuSummary, menuLikeSummary)
    }

    fun postReview(
        userId: Int,
        request: ReviewRequest,
    ): MenuDetailsDto {
        validatePartialEmptyFields(request.taste, request.price, request.foodComposition)

        val user =
            userRepository.findByIdOrNull(userId)
                ?: throw UserNotFoundException()

        val menu =
            menuRepository.findByIdOrNull(request.menuId)
                ?: throw MenuNotFoundException()

        val review: Review

        try {
            review =
                reviewRepository.save(
                    Review(
                        user = user,
                        menu = menu,
                        score = request.score,
                        comment = request.comment,
                        etc = "",
                    ),
                )
        } catch (ex: DataIntegrityViolationException) {
            throw ReviewAlreadyExistsException()
        } catch (ex: Exception) {
            throw ReviewSaveFailedException()
        }

        keywordReviewRepository.save(
            KeywordReview(
                taste = KeywordReviewUtil.getTasteLevel(request.taste),
                price = KeywordReviewUtil.getPriceLevel(request.price),
                foodComposition = KeywordReviewUtil.getFoodCompositionLevel(request.foodComposition),
                review = review,
                menu = menu,
            ),
        )

        val menuSummary = menuRepository.findMenuById(menu.id.toString())
        val menuLikeSummary =
            menuRepository.findMenuLikeByMenuIdAndUserId(
                menu.id.toString(),
                user.id.toString(),
            )

        return MenuDetailsDto.from(menuSummary, menuLikeSummary)
    }

    fun getReviews(
        userId: Int,
        menuId: Int,
        page: Int,
        size: Int,
    ): ReviewListResponse {
        val pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "created_at"))

        val menuPlainSummary = menuRepository.findPlainMenuById(menuId.toString())

        val reviews =
            reviewRepository.findByMenuIdOrderByCreatedAtDesc(
                userId,
                menuPlainSummary.getRestaurantId(),
                menuPlainSummary.getCode(),
                pageable,
            )
        val totalCount = reviewRepository.countByMenuId(menuPlainSummary.getRestaurantId(), menuPlainSummary.getCode())
        val hasNext = page * size < totalCount

        val result =
            reviews.map {
                ReviewResponse.from(it)
            }

        return ReviewListResponse(
            totalCount = totalCount.toInt(),
            hasNext = hasNext,
            result = result,
        )
    }

    fun getCommentRecommendation(score: Int): CommentRecommendationResponse {
        if (score !in 1..5) throw InvalidScoreException()

        val comment =
            reviewRepository.findRandomCommentByScore(score)
                ?: throw CommentNotFoundException()
        return CommentRecommendationResponse(comment)
    }

    fun getScoreDistribution(menuId: Int): ReviewScoreDistributionResponse {
        val menuPlainSummary: MenuPlainSummary = menuRepository.findPlainMenuById(menuId.toString())

        val counts =
            reviewRepository.findScoreCountsByMenuId(
                menuPlainSummary.getRestaurantId(),
                menuPlainSummary.getCode(),
            )

        val dist = MutableList(5) { 0 } // [0, 0, 0, 0, 0]

        counts.forEach {
            val score = (it[0] as Number).toInt()
            val count = (it[1] as Number).toInt()
            if (score in 1..5) dist[score - 1] = count
        }

        return ReviewScoreDistributionResponse(dist)
    }

    fun getKeywordScoreDistribution(menuId: Int): KeywordScoreDistributionResponse {
        val menu: MenuPlainSummary = menuRepository.findPlainMenuById(menuId.toString())
        val keywordReviewSummary: KeywordReviewSummary =
            keywordReviewRepository.findScoreCountsByRestaurantIdAndCode(menu.getRestaurantId(), menu.getCode())
                ?: return KeywordScoreDistributionResponse()
        return KeywordScoreDistributionResponse.from(keywordReviewSummary)
    }

    fun getFilteredReviews(
        userId: Int,
        menuId: Int,
        comment: Boolean?,
        etc: Boolean?,
        page: Int,
        size: Int,
    ): ReviewListResponse {
        val menuPlainSummary: MenuPlainSummary = menuRepository.findPlainMenuById(menuId.toString())

        val pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "created_at"))
        val reviews =
            reviewRepository.findFilteredReviews(
                userId,
                menuPlainSummary.getRestaurantId(),
                menuPlainSummary.getCode(),
                comment,
                etc,
                pageable,
            )
        val totalCount =
            reviewRepository.countFilteredReviews(
                menuPlainSummary.getRestaurantId(),
                menuPlainSummary.getCode(),
                comment,
                etc,
            )
        val hasNext = page * size < totalCount

        val result =
            reviews.map {
                ReviewResponse.from(it)
            }

        return ReviewListResponse(
            totalCount = totalCount.toInt(),
            hasNext = hasNext,
            result = result,
        )
    }

    fun getMyReviews(
        userId: Int,
        page: Int,
        size: Int,
    ): ReviewListResponse {
        val pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "created_at"))
        val reviews = reviewRepository.findByUserId(userId, pageable)
        val totalCount = reviewRepository.countByUserId(userId)
        val hasNext = page * size < totalCount

        val result =
            reviews.map {
                ReviewResponse.from(it)
            }

        return ReviewListResponse(
            totalCount = totalCount.toInt(),
            hasNext = hasNext,
            result = result,
        )
    }

    fun likeReview(
        reviewId: Int,
        userId: Int,
    ) {
        val user = userRepository.getReferenceById(userId)
        val review = reviewRepository.getReferenceById(reviewId)

        if (review.user == user) throw SelfReviewLikeNotAllowedException()

        val reviewLike =
            ReviewLike(
                id = 0,
                user = user,
                review = review,
            )
        reviewLikeRepository.save(reviewLike)
    }

    @Transactional
    fun unlikeReview(
        reviewId: Int,
        userId: Int,
    ): Boolean {
        val deleteCount = reviewLikeRepository.deleteByUserIdAndReviewId(userId, reviewId)
        return deleteCount > 0
    }

    private fun validatePartialEmptyFields(vararg fields: String) {
        val blankCount = fields.count { it.isBlank() }

        if (blankCount in 1..2) {
            throw KeywordMissingException()
        }
    }
}
