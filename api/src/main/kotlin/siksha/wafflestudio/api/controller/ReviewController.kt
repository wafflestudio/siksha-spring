package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.main.menu.dto.MenuDetailsDto
import siksha.wafflestudio.core.domain.main.review.dto.CommentRecommendationResponse
import siksha.wafflestudio.core.domain.main.review.dto.KeywordScoreDistributionResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewListResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewRequest
import siksha.wafflestudio.core.domain.main.review.dto.ReviewScoreDistributionResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewWithImagesRequest
import siksha.wafflestudio.core.domain.main.review.service.ReviewService

@RestController
@RequestMapping("/reviews")
class ReviewController(
    private val reviewService: ReviewService,
) {
    @PostMapping("/images", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun postReviewWithImages(
        request: HttpServletRequest,
        @RequestPart("menu_id") menuId: Int,
        @RequestPart("score") score: Int,
        @RequestPart("comment", required = false) comment: String?,
        @RequestPart("images", required = false) images: List<MultipartFile>?,
    ): MenuDetailsDto {
        val userId = request.userId
        val createDto =
            ReviewWithImagesRequest(
                menuId = menuId,
                score = score,
                comment = comment,
                images = images,
            )
        return reviewService.postReviewWithImages(userId, createDto)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun postReview(
        request: HttpServletRequest,
        @RequestBody reviewRequest: ReviewRequest,
    ): MenuDetailsDto {
        val userId = request.userId
        return reviewService.postReview(userId, reviewRequest)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getReviews(
        @RequestParam("menu_id") menuId: Int,
        @RequestParam("is_private", required = false) isPrivate: Boolean,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        request: HttpServletRequest,
    ): ReviewListResponse {
        return reviewService.getReviews(request.userId, menuId, page, size)
    }

    @GetMapping("/comments/recommendation")
    @ResponseStatus(HttpStatus.OK)
    fun getCommentRecommendation(
        @RequestParam score: Int,
    ): CommentRecommendationResponse {
        return reviewService.getCommentRecommendation(score)
    }

    @GetMapping("/dist")
    @ResponseStatus(HttpStatus.OK)
    fun getReviewScoreDistribution(
        @RequestParam("menu_id") menuId: Int,
    ): ReviewScoreDistributionResponse {
        return reviewService.getScoreDistribution(menuId)
    }

    @GetMapping("/keyword/dist")
    @ResponseStatus(HttpStatus.OK)
    fun getKeywordScoreDistribution(
        @RequestParam("menu_id") menuId: Int,
    ): KeywordScoreDistributionResponse {
        return reviewService.getKeywordScoreDistribution(menuId)
    }

    @GetMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    fun getFilteredReviews(
        @RequestParam("menu_id") menuId: Int,
        @RequestParam("comment", required = false) comment: Boolean?,
        @RequestParam("etc", required = false) etc: Boolean?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam("is_private", required = false) isPrivate: Boolean,
        request: HttpServletRequest,
    ): ReviewListResponse {
        return reviewService.getFilteredReviews(request.userId, menuId, comment, etc, page, size)
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    fun getMyReviews(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") perPage: Int,
        request: HttpServletRequest,
    ): ReviewListResponse {
        val userId = request.userId
        return reviewService.getMyReviews(userId, page, perPage)
    }

    @PostMapping("/{review_id}/like")
    @ResponseStatus(HttpStatus.CREATED)
    fun likeReview(
        @PathVariable("review_id") reviewId: Int,
        request: HttpServletRequest,
    ) = reviewService.likeReview(reviewId, request.userId)

    @DeleteMapping("/{review_id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unlikeReview(
        @PathVariable("review_id") reviewId: Int,
        request: HttpServletRequest,
    ) = reviewService.unlikeReview(reviewId, request.userId)
}
