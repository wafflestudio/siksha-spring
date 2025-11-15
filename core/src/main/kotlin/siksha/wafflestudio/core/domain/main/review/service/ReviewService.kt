package siksha.wafflestudio.core.domain.main.review.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.core.domain.common.exception.CommentNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidScoreException
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
import siksha.wafflestudio.core.domain.main.menu.dto.MenuDetailsDto
import siksha.wafflestudio.core.domain.main.menu.dto.MenuPlainSummary
import siksha.wafflestudio.core.domain.main.menu.repository.MenuRepository
import siksha.wafflestudio.core.domain.main.review.data.KeywordReview
import siksha.wafflestudio.core.domain.main.review.data.Review
import siksha.wafflestudio.core.domain.main.review.data.ReviewLike
import siksha.wafflestudio.core.domain.main.review.dto.CommentRecommendationResponse
import siksha.wafflestudio.core.domain.main.review.dto.KeywordReviewSummary
import siksha.wafflestudio.core.domain.main.review.dto.KeywordScoreDistributionResponse
import siksha.wafflestudio.core.domain.main.review.dto.MyReviewResponse
import siksha.wafflestudio.core.domain.main.review.dto.MyReviewsResponse
import siksha.wafflestudio.core.domain.main.review.dto.RestaurantWithReviewListResponse
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
import siksha.wafflestudio.core.util.EtcUtils
import siksha.wafflestudio.core.util.KeywordReviewUtil
import siksha.wafflestudio.core.util.KeywordReviewUtil.getFoodCompositionKeyword
import siksha.wafflestudio.core.util.KeywordReviewUtil.getPriceKeyword
import siksha.wafflestudio.core.util.KeywordReviewUtil.getTasteKeyword
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

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

    @Transactional
    fun postReviewWithImages(
        userId: Int,
        request: ReviewWithImagesRequest,
    ): MenuDetailsDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        val menuId = request.menu_id
        val menu = menuRepository.findByIdOrNull(menuId) ?: throw MenuNotFoundException()
        val score = request.score
        val comment = request.comment
        val tasteKeyword = request.taste
        val priceKeyword = request.price
        val foodCompositionKeyword = request.food_composition

        val images: List<MultipartFile>? =
            when (request.images) {
                is List<*> -> request.images.filterIsInstance<MultipartFile>().takeIf { it.isNotEmpty() }
                is MultipartFile -> listOf(request.images as MultipartFile)
                else -> null
            }

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
                        etc = imageUrls.takeIf { it.isNotEmpty() }?.let { objectMapper.writeValueAsString(it) },
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

    @Transactional
    fun postReview(
        userId: Int,
        request: ReviewRequest,
    ): MenuDetailsDto {
        validatePartialEmptyFields(request.taste, request.price, request.food_composition)

        val user =
            userRepository.findByIdOrNull(userId)
                ?: throw UserNotFoundException()

        val menu =
            menuRepository.findByIdOrNull(request.menu_id)
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
                        etc = null,
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
                foodComposition = KeywordReviewUtil.getFoodCompositionLevel(request.food_composition),
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

    @Transactional
    fun updateReviewWithImages(
        userId: Int,
        reviewId: Int,
        request: ReviewWithImagesRequest,
    ): MenuDetailsDto {
        // review가 존재하는지 확인
        val review = reviewRepository.findByIdOrNull(reviewId) ?: throw ReviewNotFoundException()
        if (review.user.id != userId) throw NotReviewOwnerException()
        val menuId = request.menu_id
        if (menuId != review.menu.id) throw ReviewAndMenuMismatchException()
        val score = request.score
        val comment = request.comment
        val tasteKeyword = request.taste
        val priceKeyword = request.price
        val foodCompositionKeyword = request.food_composition

        val images: List<MultipartFile>? =
            when (request.images) {
                is List<*> -> request.images.filterIsInstance<MultipartFile>().takeIf { it.isNotEmpty() }
                is MultipartFile -> listOf(request.images as MultipartFile)
                else -> null
            }

        validatePartialEmptyFields(tasteKeyword, priceKeyword, foodCompositionKeyword)

        // 기존 review에 있던 images는 삭제하고 진행
        review.etc?.let {
            val parsedImageUrls = EtcUtils.parseImageUrlsFromEtc(it)
            val keys = EtcUtils.getImageKeysFromUrlList(parsedImageUrls)
            imageRepository.softDeleteByKeyIn(keys)
        }

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

        review.score = score
        review.comment = comment ?: ""
        review.etc = imageUrls.takeIf { it.isNotEmpty() }?.let { objectMapper.writeValueAsString(it) }

        val keywordReview =
            keywordReviewRepository.findByIdOrNull(reviewId)
                ?: KeywordReview(
                    review = review,
                    menu = menuRepository.findByIdOrNull(menuId) ?: throw MenuNotFoundException(),
                    taste = -1,
                    price = -1,
                    foodComposition = -1,
                )

        keywordReview.taste = KeywordReviewUtil.getTasteLevel(tasteKeyword)
        keywordReview.price = KeywordReviewUtil.getPriceLevel(priceKeyword)
        keywordReview.foodComposition = KeywordReviewUtil.getFoodCompositionLevel(foodCompositionKeyword)

        keywordReviewRepository.save(keywordReview)

        val menuSummary = menuRepository.findMenuById(menuId.toString())
        val menuLikeSummary =
            menuRepository.findMenuLikeByMenuIdAndUserId(
                menuId.toString(),
                userId.toString(),
            )

        return MenuDetailsDto.from(menuSummary, menuLikeSummary)
    }

    @Transactional
    fun deleteReview(
        userId: Int,
        reviewId: Int,
    ) {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        val review = reviewRepository.findByIdOrNull(reviewId) ?: throw ReviewNotFoundException()
        if (review.user.id != userId) {
            throw NotReviewOwnerException()
        }

        review.etc?.let {
            val parsedImageUrls = EtcUtils.parseImageUrlsFromEtc(it)
            val keys = EtcUtils.getImageKeysFromUrlList(parsedImageUrls)
            imageRepository.softDeleteByKeyIn(keys)
        }

        if (keywordReviewRepository.existsById(reviewId)) {
            keywordReviewRepository.deleteById(reviewId)
        }
        reviewRepository.deleteById(reviewId)
    }

    fun getReview(
        reviewId: Int,
        userId: Int?,
    ): MyReviewResponse {
        val review = reviewRepository.findByIdOrNull(reviewId) ?: throw ReviewNotFoundException()
        val keywordReview = keywordReviewRepository.findByIdOrNull(reviewId)
        val keywordReviews =
            if (keywordReview == null) {
                listOf(null, null, null)
            } else {
                listOf(
                    getTasteKeyword(keywordReview.taste),
                    getPriceKeyword(keywordReview.price),
                    getFoodCompositionKeyword(keywordReview.foodComposition),
                )
            }

        // Check if user liked this review (false for anonymous users with userId = 0)
        val isLiked =
            if (userId == null) {
                false
            } else {
                reviewLikeRepository.existsByUserIdAndReviewId(userId, reviewId)
            }

        return MyReviewResponse(
            id = review.id,
            menuId = review.menu.id,
            nameKr = review.menu.nameKr,
            nameEn = review.menu.nameEn,
            userId = review.user.id,
            score = review.score,
            comment = review.comment,
            etc = review.etc,
            createdAt = review.updatedAt,
            updatedAt = review.updatedAt,
            keywordReviews = keywordReviews,
            isLiked = isLiked,
        )
    }

    fun getReviews(
        userId: Int?,
        menuId: Int,
        page: Int,
        size: Int,
    ): ReviewListResponse {
        // userId가 null인 경우 비로그인 -> is_liked = false
        val targetUserId = userId ?: 0
        val pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "created_at"))

        val menuPlainSummary = menuRepository.findPlainMenuById(menuId.toString())

        val reviews =
            reviewRepository.findByMenuIdOrderByCreatedAtDesc(
                targetUserId,
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

        return KeywordScoreDistributionResponse.from(keywordReviewSummary)
    }

    fun getFilteredReviews(
        userId: Int?,
        menuId: Int,
        comment: Boolean?,
        image: Boolean?,
        page: Int,
        size: Int,
    ): ReviewListResponse {
        // userId가 null인 경우 비로그인 -> is_liked = false
        val targetUserId = userId ?: 0
        val menuPlainSummary: MenuPlainSummary = menuRepository.findPlainMenuById(menuId.toString())

        val pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "created_at"))
        val reviews =
            reviewRepository.findFilteredReviews(
                targetUserId,
                menuPlainSummary.getRestaurantId(),
                menuPlainSummary.getCode(),
                comment,
                image,
                pageable,
            )
        val totalCount =
            reviewRepository.countFilteredReviews(
                menuPlainSummary.getRestaurantId(),
                menuPlainSummary.getCode(),
                comment,
                image,
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
    ): MyReviewsResponse {
        val totalRestaurant = reviewRepository.countDistinctRestaurantsByUserId(userId)
        if (totalRestaurant == 0L) {
            return MyReviewsResponse(
                totalCount = 0,
                hasNext = false,
                result = emptyList(),
            )
        }

        val pageable = PageRequest.of(page - 1, size)
        val restaurantIds = reviewRepository.findRestaurantIdsByUserIdPaged(userId, pageable)

        if (restaurantIds.isEmpty()) {
            val total = totalRestaurant.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            return MyReviewsResponse(
                totalCount = total,
                hasNext = page * size < total,
                result = emptyList(),
            )
        }

        val reviews = reviewRepository.findAllByUserIdAndRestaurantIds(userId, restaurantIds)

        val reviewIds = reviews.map { it.id }
        val keywordByReviewId =
            keywordReviewRepository
                .findAllById(reviewIds)
                .associateBy { it.id }

        val bucket = linkedMapOf<Int, MutableList<Review>>()
        restaurantIds.forEach { bucket[it] = mutableListOf() }
        for (r in reviews) {
            val rid = r.menu.restaurant.id
            bucket[rid]?.add(r)
        }

        val grouped =
            bucket.entries.mapNotNull { (rid, list) ->
                if (list.isEmpty()) return@mapNotNull null
                val rest = list.first().menu.restaurant
                RestaurantWithReviewListResponse(
                    restaurantId = rid,
                    nameKr = rest.nameKr,
                    nameEn = rest.nameEn,
                    reviews =
                        list.map { it ->
                            val keywordReview = keywordByReviewId[it.id]
                            val keywordReviews =
                                if (keywordReview != null) {
                                    listOf(
                                        getTasteKeyword(keywordReview.taste),
                                        getPriceKeyword(keywordReview.price),
                                        getFoodCompositionKeyword(keywordReview.foodComposition),
                                    )
                                } else {
                                    listOf(null, null, null)
                                }

                            MyReviewResponse(
                                id = it.id,
                                menuId = it.menu.id,
                                nameKr = it.menu.nameKr,
                                nameEn = it.menu.nameEn,
                                userId = userId,
                                score = it.score,
                                comment = it.comment,
                                etc = it.etc,
                                createdAt = it.createdAt,
                                updatedAt = it.updatedAt,
                                keywordReviews = keywordReviews,
                                isLiked = false,
                            )
                        },
                )
            }

        val total = totalRestaurant.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val hasNext = page * size < total

        return MyReviewsResponse(
            totalCount = total,
            hasNext = hasNext,
            result = grouped,
        )
    }

    @Transactional
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

        // 키워드 리뷰는 0개 혹은 3개만 허용
        if (blankCount in 1..2) {
            throw KeywordMissingException()
        }
    }
}
