package siksha.wafflestudio.api.controller.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2DetailsDto
import siksha.wafflestudio.core.domain.main.review.dto.KeywordReviewV2ScoreDistributionResponse
import siksha.wafflestudio.core.domain.main.review.dto.MyReviewV2Response
import siksha.wafflestudio.core.domain.main.review.dto.MyReviewsV2Response
import siksha.wafflestudio.core.domain.main.review.dto.ReviewV2ListResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewV2Request
import siksha.wafflestudio.core.domain.main.review.dto.ReviewV2ScoreDistributionResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewV2WithImagesRequest
import siksha.wafflestudio.core.domain.main.review.service.ReviewV2Service

@RestController
@RequestMapping("/v2/reviews")
@Tag(name = "Reviews-V2", description = "Review V2 endpoints")
class ReviewV2Controller(
    private val reviewService: ReviewV2Service,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create V2 review", description = "Create a review for a normalized V2 menu")
    @SecurityRequirement(name = "bearerAuth")
    fun postReview(
        request: HttpServletRequest,
        @RequestBody reviewRequest: ReviewV2Request,
    ): MenuV2DetailsDto = reviewService.postReview(request.userId, reviewRequest)

    @PostMapping("/images", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create V2 review with images", description = "Create a review with images for a normalized V2 menu")
    @SecurityRequirement(name = "bearerAuth")
    fun postReviewWithImages(
        request: HttpServletRequest,
        @ModelAttribute reviewRequest: ReviewV2WithImagesRequest,
    ): MenuV2DetailsDto = reviewService.postReviewWithImages(request.userId, reviewRequest)

    @PatchMapping("/{reviewId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update V2 review", description = "Update a review with optional image replacement")
    @SecurityRequirement(name = "bearerAuth")
    fun updateReviewWithImages(
        request: HttpServletRequest,
        @PathVariable reviewId: Long,
        @ModelAttribute reviewRequest: ReviewV2WithImagesRequest,
    ): MenuV2DetailsDto = reviewService.updateReviewWithImages(request.userId, reviewId, reviewRequest)

    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete V2 review", description = "Delete a V2 review")
    @SecurityRequirement(name = "bearerAuth")
    fun deleteReview(
        request: HttpServletRequest,
        @PathVariable reviewId: Long,
    ) {
        reviewService.deleteReview(request.userId, reviewId)
    }

    @PostMapping("/{reviewId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Like V2 review", description = "Like a V2 review")
    @SecurityRequirement(name = "bearerAuth")
    fun likeReview(
        @PathVariable reviewId: Long,
        request: HttpServletRequest,
    ) = reviewService.likeReview(reviewId, request.userId)

    @DeleteMapping("/{reviewId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unlike V2 review", description = "Unlike a V2 review")
    @SecurityRequirement(name = "bearerAuth")
    fun unlikeReview(
        @PathVariable reviewId: Long,
        request: HttpServletRequest,
    ) {
        reviewService.unlikeReview(reviewId, request.userId)
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get my V2 reviews", description = "Get reviews created by the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    fun getMyReviews(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") perPage: Int,
        request: HttpServletRequest,
    ): MyReviewsV2Response = reviewService.getMyReviews(request.userId, page, perPage)

    @GetMapping("/dist")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get V2 review score distribution", description = "Get score distribution for a normalized V2 menu")
    fun getReviewScoreDistribution(
        @RequestParam("menu_id") menuId: Long,
    ): ReviewV2ScoreDistributionResponse = reviewService.getScoreDistribution(menuId)

    @GetMapping("/keyword/dist")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get V2 keyword score distribution", description = "Get keyword distribution for a normalized V2 menu")
    fun getKeywordScoreDistribution(
        @RequestParam("menu_id") menuId: Long,
    ): KeywordReviewV2ScoreDistributionResponse = reviewService.getKeywordScoreDistribution(menuId)

    @GetMapping("/{reviewId}/web")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get web V2 review detail", description = "Get V2 review detail for unauthenticated clients")
    fun getReviewWithoutAuth(
        @PathVariable reviewId: Long,
    ): MyReviewV2Response = reviewService.getReview(reviewId = reviewId, userId = null)

    @GetMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get V2 review detail", description = "Get V2 review detail")
    @SecurityRequirement(name = "bearerAuth")
    fun getReviewLogin(
        @PathVariable reviewId: Long,
        request: HttpServletRequest,
    ): MyReviewV2Response = reviewService.getReview(reviewId = reviewId, userId = request.userId)

    @GetMapping("/web")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get web V2 reviews", description = "Get V2 reviews for a normalized menu")
    fun getReviewsWithoutAuth(
        @RequestParam("menu_id") menuId: Long,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ReviewV2ListResponse = reviewService.getReviews(userId = null, menuId = menuId, page = page, size = size)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get V2 reviews", description = "Get V2 reviews for a normalized menu")
    @SecurityRequirement(name = "bearerAuth")
    fun getReviewsLogin(
        @RequestParam("menu_id") menuId: Long,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        request: HttpServletRequest,
    ): ReviewV2ListResponse = reviewService.getReviews(userId = request.userId, menuId = menuId, page = page, size = size)

    @GetMapping("/filter/web")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get filtered web V2 reviews", description = "Get filtered V2 reviews for unauthenticated clients")
    fun getFilteredReviews(
        @RequestParam("menu_id") menuId: Long,
        @RequestParam("comment", required = false) comment: Boolean?,
        @RequestParam("image", required = false) image: Boolean?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ReviewV2ListResponse =
        reviewService.getFilteredReviews(userId = null, menuId = menuId, comment = comment, image = image, page = page, size = size)

    @GetMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get filtered V2 reviews", description = "Get filtered V2 reviews")
    @SecurityRequirement(name = "bearerAuth")
    fun getFilteredReviewsLogin(
        @RequestParam("menu_id") menuId: Long,
        @RequestParam("comment", required = false) comment: Boolean?,
        @RequestParam("image", required = false) image: Boolean?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        request: HttpServletRequest,
    ): ReviewV2ListResponse =
        reviewService.getFilteredReviews(
            userId = request.userId,
            menuId = menuId,
            comment = comment,
            image = image,
            page = page,
            size = size,
        )
}
