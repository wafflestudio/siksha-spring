package siksha.wafflestudio.core.domain.main.review.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.CommentNotFoundException
import siksha.wafflestudio.core.domain.common.exception.InvalidScoreException
import siksha.wafflestudio.core.domain.common.exception.MenuNotFoundException
import siksha.wafflestudio.core.domain.common.exception.UserNotFoundException
import siksha.wafflestudio.core.domain.image.data.Image
import siksha.wafflestudio.core.domain.image.data.ImageCategory
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuRepository
import siksha.wafflestudio.core.domain.main.review.data.Review
import siksha.wafflestudio.core.domain.main.review.dto.CommentRecommendationResponse
import siksha.wafflestudio.core.domain.main.menu.dto.MenuDetailsDto
import siksha.wafflestudio.core.domain.main.review.dto.ReviewResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewScoreDistributionResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewWithImagesRequest
import siksha.wafflestudio.core.domain.main.review.dto.ReviewListResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewRequest
import siksha.wafflestudio.core.domain.main.review.repository.ReviewRepository
import siksha.wafflestudio.core.domain.user.repository.UserRepository
import siksha.wafflestudio.core.infrastructure.s3.S3ImagePrefix
import siksha.wafflestudio.core.infrastructure.s3.S3Service
import siksha.wafflestudio.core.util.EtcUtils
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val menuRepository: MenuRepository,
    private val imageRepository: ImageRepository,
    private val s3Service: S3Service,
) {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    fun postReviewWithImages(
        userId: Int,
        reviewWithImagesRequest: ReviewWithImagesRequest
    ): MenuDetailsDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        val menuId = reviewWithImagesRequest.menuId
        val menu = menuRepository.findByIdOrNull(menuId) ?: throw MenuNotFoundException()
        val score = reviewWithImagesRequest.score
        val comment = reviewWithImagesRequest.comment
        val images = reviewWithImagesRequest.images

        val uploadedFiles = images?.takeIf { it.isNotEmpty() }?.let {
            s3Service.uploadFiles(
                it,
                S3ImagePrefix.REVIEW,
                "menu-$menuId/user-$userId/review-${OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))}"
            )
        } ?: emptyList()

        imageRepository.saveAll(
            uploadedFiles.map { dto ->
                Image(
                    key = dto.key,
                    category = ImageCategory.REVIEW,
                    userId = userId,
                    isDeleted = false
                )
            }
        )

        val imageUrls = uploadedFiles.map { it.url }

        val review = reviewRepository.save(
            Review(
                user = user,
                menu = menu,
                score = score,
                comment = comment ?: "",
                etc = objectMapper.writeValueAsString(imageUrls) // 이미지 URL 들을 JSON array로 저장
            )
        )

        val menuSummary = menuRepository.findMenuById(menu.id.toString())
        val menuLikeSummary = menuRepository.findMenuLikeByMenuIdAndUserId(
            menu.id.toString(),
            user.id.toString()
        )

        return MenuDetailsDto(
            createdAt = menuSummary.getCreatedAt(),
            updatedAt = menuSummary.getUpdatedAt(),
            id = menuSummary.getId(),
            restaurantId = menuSummary.getRestaurantId(),
            code = menuSummary.getCode(),
            date = menuSummary.getDate(),
            type = menuSummary.getType(),
            nameKr = menuSummary.getNameKr(),
            nameEn = menuSummary.getNameEn(),
            price = menuSummary.getPrice(),
            etc = EtcUtils.convertMenuEtc(menuSummary.getEtc()),
            score = menuSummary.getScore(),
            reviewCnt = menuSummary.getReviewCnt(),
            isLiked = menuLikeSummary.getIsLiked(),
            likeCnt = menuLikeSummary.getLikeCnt()
        )
    }

    fun postReview(userId: Int, request: ReviewRequest): MenuDetailsDto {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()

        val menu = menuRepository.findByIdOrNull(request.menuId)
            ?: throw MenuNotFoundException()

        val review = reviewRepository.save(
            Review(
                user = user,
                menu = menu,
                score = request.score,
                comment = request.comment,
                etc = ""
            )
        )

        val menuSummary = menuRepository.findMenuById(menu.id.toString())
        val menuLikeSummary = menuRepository.findMenuLikeByMenuIdAndUserId(
            menu.id.toString(),
            user.id.toString()
        )

        return MenuDetailsDto(
            createdAt = review.createdAt,
            updatedAt = review.updatedAt,
            id = menuSummary.getId(),
            restaurantId = menuSummary.getRestaurantId(),
            code = menuSummary.getCode(),
            date = menuSummary.getDate(),
            type = menuSummary.getType(),
            nameKr = menuSummary.getNameKr(),
            nameEn = menuSummary.getNameEn(),
            price = menuSummary.getPrice(),
            etc = EtcUtils.convertMenuEtc(menuSummary.getEtc()),
            score = menuSummary.getScore(),
            reviewCnt = menuSummary.getReviewCnt(),
            isLiked = menuLikeSummary.getIsLiked(),
            likeCnt = menuLikeSummary.getLikeCnt()
        )
    }


    fun getReviews(menuId: Int, page: Int, size: Int): ReviewListResponse {
        val pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val reviews = reviewRepository.findByMenuIdOrderByCreatedAtDesc(menuId, pageable)
        val totalCount = reviewRepository.countByMenuId(menuId)
        val hasNext = page * size < totalCount

        val result = reviews.map {
            ReviewResponse(
                id = it.id,
                menuId = menuId,
                userId = it.user.id,
                score = it.score,
                comment = it.comment,
                etc = it.etc,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }

        return ReviewListResponse(
            totalCount = totalCount.toInt(),
            hasNext = hasNext,
            result = result
        )
    }


    fun getCommentRecommendation(score: Int): CommentRecommendationResponse {
        if (score !in 1..5) throw InvalidScoreException()

        val comment = reviewRepository.findRandomCommentByScore(score)
            ?: throw CommentNotFoundException()
        return CommentRecommendationResponse(comment)
    }

    fun getScoreDistribution(menuId: Int): ReviewScoreDistributionResponse {
        val counts = reviewRepository.findScoreCountsByMenuId(menuId)

        val dist = MutableList(5) { 0 }  // [0, 0, 0, 0, 0]

        counts.forEach {
            val score = (it[0] as Number).toInt()
            val count = (it[1] as Number).toInt()
            if (score in 1..5) dist[score - 1] = count
        }

        return ReviewScoreDistributionResponse(dist)
    }


    fun getFilteredReviews(
        menuId: Int,
        comment: Boolean?,
        etc: Boolean?,
        page: Int,
        size: Int
    ): ReviewListResponse {
        val offset = (page - 1) * size
        val reviews = reviewRepository.findFilteredReviews(menuId, comment, etc, size, offset)
        val totalCount = reviewRepository.countFilteredReviews(menuId, comment, etc)
        val hasNext = page * size < totalCount

        val result = reviews.map {
            ReviewResponse(
                id = it.id,
                menuId = menuId,
                userId = it.user.id,
                score = it.score,
                comment = it.comment,
                etc = it.etc,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }

        return ReviewListResponse(
            totalCount = totalCount.toInt(),
            hasNext = hasNext,
            result = result
        )
    }

    fun getMyReviews(userId: Int, page: Int, size: Int): ReviewListResponse {
        val offset = (page - 1) * size
        val reviews = reviewRepository.findByUserId(userId, size, offset)
        val totalCount = reviewRepository.countByUserId(userId)
        val hasNext = page * size < totalCount

        val result = reviews.map {
            ReviewResponse(
                id = it.id,
                menuId = it.menu.id,
                userId = userId,
                score = it.score,
                comment = it.comment,
                etc = it.etc,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }

        return ReviewListResponse(
            totalCount = totalCount.toInt(),
            hasNext = hasNext,
            result = result
        )
    }
}
