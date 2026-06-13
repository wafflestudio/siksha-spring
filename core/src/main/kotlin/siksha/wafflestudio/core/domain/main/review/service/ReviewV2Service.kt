package siksha.wafflestudio.core.domain.main.review.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.common.exception.KeywordMissingException
import siksha.wafflestudio.core.domain.common.exception.MenuNotFoundException
import siksha.wafflestudio.core.domain.common.exception.NotReviewOwnerException
import siksha.wafflestudio.core.domain.common.exception.ReviewAlreadyExistsException
import siksha.wafflestudio.core.domain.common.exception.ReviewAndMenuMismatchException
import siksha.wafflestudio.core.domain.common.exception.ReviewNotFoundException
import siksha.wafflestudio.core.domain.common.exception.ReviewSaveFailedException
import siksha.wafflestudio.core.domain.common.exception.SelfReviewLikeNotAllowedException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.image.data.Image
import siksha.wafflestudio.core.domain.image.data.ImageCategory
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2DetailsDto
import siksha.wafflestudio.core.domain.main.menu.repository.MenuV2Repository
import siksha.wafflestudio.core.domain.main.menu.service.MenuV2Service
import siksha.wafflestudio.core.domain.main.review.data.KeywordReviewV2
import siksha.wafflestudio.core.domain.main.review.data.ReviewLikeV2
import siksha.wafflestudio.core.domain.main.review.data.ReviewV2
import siksha.wafflestudio.core.domain.main.review.dto.KeywordReviewV2ScoreDistributionResponse
import siksha.wafflestudio.core.domain.main.review.dto.MyReviewV2Response
import siksha.wafflestudio.core.domain.main.review.dto.MyReviewsV2Response
import siksha.wafflestudio.core.domain.main.review.dto.RestaurantWithReviewV2ListResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewV2ListResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewV2Request
import siksha.wafflestudio.core.domain.main.review.dto.ReviewV2Response
import siksha.wafflestudio.core.domain.main.review.dto.ReviewV2ScoreDistributionResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewV2WithImagesRequest
import siksha.wafflestudio.core.domain.main.review.repository.KeywordReviewV2Repository
import siksha.wafflestudio.core.domain.main.review.repository.ReviewLikeV2Repository
import siksha.wafflestudio.core.domain.main.review.repository.ReviewV2Repository
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.imageupload.ImagePrefix
import siksha.wafflestudio.core.infrastructure.imageupload.ImageUploadUseCase
import siksha.wafflestudio.core.util.EtcUtils
import siksha.wafflestudio.core.util.KeywordReviewUtil
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class ReviewV2Service(
    private val reviewRepository: ReviewV2Repository,
    private val userRepository: UserRepository,
    private val menuRepository: MenuV2Repository,
    private val menuService: MenuV2Service,
    private val imageRepository: ImageRepository,
    private val keywordReviewRepository: KeywordReviewV2Repository,
    private val reviewLikeRepository: ReviewLikeV2Repository,
    private val imageUploadUseCase: ImageUploadUseCase,
) {
    @Transactional
    fun postReview(
        userId: Int,
        request: ReviewV2Request,
    ): MenuV2DetailsDto {
        validatePartialEmptyFields(request.taste, request.price, request.food_composition)
        return createReview(
            userId = userId,
            menuId = request.menu_id,
            score = request.score,
            comment = request.comment,
            tasteKeyword = request.taste,
            priceKeyword = request.price,
            foodCompositionKeyword = request.food_composition,
            images = null,
        )
    }

    @Transactional
    fun postReviewWithImages(
        userId: Int,
        request: ReviewV2WithImagesRequest,
    ): MenuV2DetailsDto {
        validatePartialEmptyFields(request.taste, request.price, request.food_composition)
        return createReview(
            userId = userId,
            menuId = request.menu_id,
            score = request.score,
            comment = request.comment,
            tasteKeyword = request.taste,
            priceKeyword = request.price,
            foodCompositionKeyword = request.food_composition,
            images = extractImages(request.images),
        )
    }

    @Transactional
    fun updateReviewWithImages(
        userId: Int,
        reviewId: Long,
        request: ReviewV2WithImagesRequest,
    ): MenuV2DetailsDto {
        validatePartialEmptyFields(request.taste, request.price, request.food_composition)
        val review = reviewRepository.findByIdForDetail(reviewId) ?: throw ReviewNotFoundException()
        if (review.user.id != userId) throw NotReviewOwnerException()
        if (request.menu_id != review.menu.id) throw ReviewAndMenuMismatchException()

        review.etc?.let { softDeleteReviewImages(it) }

        val imageUrls = uploadReviewImages(request.menu_id, userId, extractImages(request.images))
        review.score = request.score
        review.comment = request.comment ?: ""
        review.etc = imageUrls.takeIf { it.isNotEmpty() }?.let { EtcUtils.convertImageUrlsToEtcJson(it) }

        val keywordReview =
            keywordReviewRepository.findByIdOrNull(reviewId)
                ?: KeywordReviewV2(review = review)
        keywordReview.taste = KeywordReviewUtil.getTasteLevel(request.taste) ?: -1
        keywordReview.price = KeywordReviewUtil.getPriceLevel(request.price) ?: -1
        keywordReview.foodComposition = KeywordReviewUtil.getFoodCompositionLevel(request.food_composition) ?: -1
        keywordReviewRepository.save(keywordReview)

        return menuService.getMenuById(menuId = review.menu.id, userId = userId)
    }

    @Transactional
    fun deleteReview(
        userId: Int,
        reviewId: Long,
    ) {
        userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        val review = reviewRepository.findByIdForDetail(reviewId) ?: throw ReviewNotFoundException()
        if (review.user.id != userId) throw NotReviewOwnerException()

        review.etc?.let { softDeleteReviewImages(it) }
        if (keywordReviewRepository.existsById(reviewId)) {
            keywordReviewRepository.deleteById(reviewId)
        }
        reviewRepository.deleteById(reviewId)
    }

    fun getReview(
        reviewId: Long,
        userId: Int?,
    ): MyReviewV2Response {
        val review = reviewRepository.findByIdForDetail(reviewId) ?: throw ReviewNotFoundException()
        val keywordReview = keywordReviewRepository.findByIdOrNull(reviewId)
        val isLiked = userId?.let { reviewLikeRepository.existsByUserIdAndReviewId(it, reviewId) } ?: false
        return MyReviewV2Response.from(review, keywordReview, isLiked)
    }

    fun getReviews(
        userId: Int?,
        menuId: Long,
        page: Int,
        size: Int,
    ): ReviewV2ListResponse {
        ensureMenuExists(menuId)
        val targetUserId = userId ?: 0
        val pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "created_at"))
        val reviews = reviewRepository.findByMenuIdOrderByCreatedAtDesc(targetUserId, menuId, pageable)
        val totalCount = reviewRepository.countByMenuId(menuId)
        val hasNext = page * size < totalCount

        return ReviewV2ListResponse(
            totalCount = totalCount.toInt(),
            hasNext = hasNext,
            result = reviews.map(ReviewV2Response::from),
        )
    }

    fun getFilteredReviews(
        userId: Int?,
        menuId: Long,
        comment: Boolean?,
        image: Boolean?,
        page: Int,
        size: Int,
    ): ReviewV2ListResponse {
        ensureMenuExists(menuId)
        val targetUserId = userId ?: 0
        val pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "created_at"))
        val reviews = reviewRepository.findFilteredReviews(targetUserId, menuId, comment, image, pageable)
        val totalCount = reviewRepository.countFilteredReviews(menuId, comment, image)
        val hasNext = page * size < totalCount

        return ReviewV2ListResponse(
            totalCount = totalCount.toInt(),
            hasNext = hasNext,
            result = reviews.map(ReviewV2Response::from),
        )
    }

    fun getScoreDistribution(menuId: Long): ReviewV2ScoreDistributionResponse {
        ensureMenuExists(menuId)
        val dist = MutableList(5) { 0 }
        reviewRepository.findScoreCountsByMenuId(menuId).forEach {
            val score = (it[0] as Number).toInt()
            val count = (it[1] as Number).toInt()
            if (score in 1..5) dist[score - 1] = count
        }
        return ReviewV2ScoreDistributionResponse(dist)
    }

    fun getKeywordScoreDistribution(menuId: Long): KeywordReviewV2ScoreDistributionResponse {
        ensureMenuExists(menuId)
        return KeywordReviewV2ScoreDistributionResponse.from(keywordReviewRepository.findScoreCountsByMenuId(menuId))
    }

    fun getMyReviews(
        userId: Int,
        page: Int,
        size: Int,
    ): MyReviewsV2Response {
        userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        val totalRestaurant = reviewRepository.countDistinctRestaurantsByUserId(userId)
        if (totalRestaurant == 0L) {
            return MyReviewsV2Response(totalCount = 0, hasNext = false, result = emptyList())
        }

        val pageable = PageRequest.of(page - 1, size)
        val restaurantIds = reviewRepository.findRestaurantIdsByUserIdPaged(userId, pageable)
        if (restaurantIds.isEmpty()) {
            val total = totalRestaurant.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            return MyReviewsV2Response(totalCount = total, hasNext = page * size < total, result = emptyList())
        }

        val reviews = reviewRepository.findAllByUserIdAndRestaurantIds(userId, restaurantIds)
        val keywordByReviewId = keywordReviewRepository.findAllById(reviews.map { it.id }).associateBy { it.id }
        val bucket = linkedMapOf<Int, MutableList<ReviewV2>>()
        restaurantIds.forEach { bucket[it] = mutableListOf() }
        reviews.forEach { review -> bucket[review.menu.restaurant.id]?.add(review) }

        val grouped =
            bucket.entries.mapNotNull { (restaurantId, list) ->
                if (list.isEmpty()) return@mapNotNull null
                val restaurant = list.first().menu.restaurant
                RestaurantWithReviewV2ListResponse(
                    restaurantId = restaurantId,
                    restaurantName = restaurant.name,
                    reviews = list.map { MyReviewV2Response.from(it, keywordByReviewId[it.id], isLiked = false) },
                )
            }

        val total = totalRestaurant.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        return MyReviewsV2Response(
            totalCount = total,
            hasNext = page * size < total,
            result = grouped,
        )
    }

    @Transactional
    fun likeReview(
        reviewId: Long,
        userId: Int,
    ) {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        val review = reviewRepository.findByIdForDetail(reviewId) ?: throw ReviewNotFoundException()
        if (review.user.id == userId) throw SelfReviewLikeNotAllowedException()
        if (!reviewLikeRepository.existsByUserIdAndReviewId(userId, reviewId)) {
            reviewLikeRepository.save(ReviewLikeV2(user = user, review = review))
        }
    }

    @Transactional
    fun unlikeReview(
        reviewId: Long,
        userId: Int,
    ): Boolean = reviewLikeRepository.deleteByUserIdAndReviewId(userId, reviewId) > 0

    private fun createReview(
        userId: Int,
        menuId: Long,
        score: Int,
        comment: String?,
        tasteKeyword: String,
        priceKeyword: String,
        foodCompositionKeyword: String,
        images: List<MultipartFile>?,
    ): MenuV2DetailsDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        val menu = menuRepository.findByIdOrNull(menuId) ?: throw MenuNotFoundException()
        if (reviewRepository.existsByMenuIdAndUserId(menuId, userId)) {
            throw ReviewAlreadyExistsException()
        }

        val imageUrls = uploadReviewImages(menuId, userId, images)
        val review =
            try {
                reviewRepository.save(
                    ReviewV2(
                        user = user,
                        menu = menu,
                        score = score,
                        comment = comment ?: "",
                        etc = imageUrls.takeIf { it.isNotEmpty() }?.let { EtcUtils.convertImageUrlsToEtcJson(it) },
                    ),
                )
            } catch (ex: Exception) {
                throw ReviewSaveFailedException()
            }

        keywordReviewRepository.save(
            KeywordReviewV2(
                taste = KeywordReviewUtil.getTasteLevel(tasteKeyword) ?: -1,
                price = KeywordReviewUtil.getPriceLevel(priceKeyword) ?: -1,
                foodComposition = KeywordReviewUtil.getFoodCompositionLevel(foodCompositionKeyword) ?: -1,
                review = review,
            ),
        )

        return menuService.getMenuById(menuId = menu.id, userId = user.id)
    }

    private fun uploadReviewImages(
        menuId: Long,
        userId: Int,
        images: List<MultipartFile>?,
    ): List<String> {
        val uploadedFiles =
            images?.takeIf { it.isNotEmpty() }?.let {
                imageUploadUseCase.uploadFiles(
                    it,
                    ImagePrefix.REVIEW,
                    "menu-$menuId/user-$userId/review-${OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))}",
                )
            } ?: emptyList()

        if (uploadedFiles.isNotEmpty()) {
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
        }

        return uploadedFiles.map { it.url }
    }

    private fun extractImages(images: Any?): List<MultipartFile>? =
        when (images) {
            is List<*> -> images.filterIsInstance<MultipartFile>().takeIf { it.isNotEmpty() }
            is MultipartFile -> listOf(images)
            else -> null
        }

    private fun softDeleteReviewImages(etc: String) {
        val parsedImageUrls = EtcUtils.parseImageUrlsFromEtc(etc)
        val keys = EtcUtils.getImageKeysFromUrlList(parsedImageUrls)
        if (keys.isNotEmpty()) {
            imageRepository.softDeleteByKeyIn(keys)
        }
    }

    private fun ensureMenuExists(menuId: Long) {
        if (!menuRepository.existsById(menuId)) {
            throw MenuNotFoundException()
        }
    }

    private fun validatePartialEmptyFields(vararg fields: String) {
        val blankCount = fields.count { it.isBlank() }
        if (blankCount in 1..2) {
            throw KeywordMissingException()
        }
    }
}
