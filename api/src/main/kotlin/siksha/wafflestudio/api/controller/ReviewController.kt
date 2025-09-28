package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
import siksha.wafflestudio.api.common.filter.userId
import siksha.wafflestudio.core.domain.main.menu.dto.MenuDetailsDto
import siksha.wafflestudio.core.domain.main.review.dto.CommentRecommendationResponse
import siksha.wafflestudio.core.domain.main.review.dto.KeywordScoreDistributionResponse
import siksha.wafflestudio.core.domain.main.review.dto.MyReviewResponse
import siksha.wafflestudio.core.domain.main.review.dto.MyReviewsResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewListResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewRequest
import siksha.wafflestudio.core.domain.main.review.dto.ReviewScoreDistributionResponse
import siksha.wafflestudio.core.domain.main.review.dto.ReviewWithImagesRequest
import siksha.wafflestudio.core.domain.main.review.service.ReviewService

@RestController
@RequestMapping("/reviews")
@Tag(name = "Reviews", description = "리뷰 관리 엔드포인트")
class ReviewController(
    private val reviewService: ReviewService,
) {
    // ============================================================================
    // POST/PATCH/DELETE Endpoints (Always Require Authentication)
    // ============================================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "새 리뷰 작성", description = "메뉴에 대한 새로운 리뷰를 작성합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun postReview(
        request: HttpServletRequest,
        @RequestBody reviewRequest: ReviewRequest,
    ): MenuDetailsDto {
        val userId = request.userId
        return reviewService.postReview(userId, reviewRequest)
    }

    @PostMapping("/images", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "이미지와 함께 새 리뷰 작성", description = "이미지 업로드와 함께 메뉴에 대한 새로운 리뷰를 작성합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun postReviewWithImages(
        request: HttpServletRequest,
        @ModelAttribute reviewRequest: ReviewWithImagesRequest,
    ): MenuDetailsDto {
        val userId = request.userId
        return reviewService.postReviewWithImages(userId, reviewRequest)
    }

    @PostMapping("/{review_id}/like")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "리뷰 좋아요", description = "특정 리뷰에 좋아요를 누릅니다")
    @SecurityRequirement(name = "bearerAuth")
    fun likeReview(
        @PathVariable("review_id") reviewId: Int,
        request: HttpServletRequest,
    ) = reviewService.likeReview(reviewId, request.userId)

    @PatchMapping("/{review_id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "이미지와 함께 리뷰 수정", description = "이미지 업로드와 함께 기존 리뷰를 수정합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun updateReviewWithImages(
        request: HttpServletRequest,
        @PathVariable("review_id") reviewId: Int,
        @ModelAttribute reviewRequest: ReviewWithImagesRequest,
    ): MenuDetailsDto {
        val userId = request.userId
        return reviewService.updateReviewWithImages(userId, reviewId, reviewRequest)
    }

    @DeleteMapping("/{review_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "리뷰 삭제", description = "기존 리뷰를 삭제합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun deleteReview(
        request: HttpServletRequest,
        @PathVariable("review_id") reviewId: Int,
    ) {
        val userId = request.userId
        reviewService.deleteReview(userId, reviewId)
    }

    @DeleteMapping("/{review_id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "리뷰 좋아요 취소", description = "특정 리뷰의 좋아요를 취소합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun unlikeReview(
        @PathVariable("review_id") reviewId: Int,
        request: HttpServletRequest,
    ) = reviewService.unlikeReview(reviewId, request.userId)

    // ============================================================================
    // GET Endpoints (Authentication Required)
    // ============================================================================

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "내 리뷰 조회", description = "인증된 사용자가 작성한 리뷰를 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun getMyReviews(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") perPage: Int,
        request: HttpServletRequest,
    ): MyReviewsResponse {
        val userId = request.userId
        return reviewService.getMyReviews(userId, page, perPage)
    }

    // ============================================================================
    // GET Endpoints (No Authentication Required)
    // ============================================================================

    @GetMapping("/comments/recommendation")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "댓글 추천 조회", description = "점수 기반 추천 댓글을 조회합니다 (인증 불필요)")
    fun getCommentRecommendation(
        @RequestParam score: Int,
    ): CommentRecommendationResponse {
        return reviewService.getCommentRecommendation(score)
    }

    @GetMapping("/dist")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "리뷰 점수 분포 조회", description = "메뉴의 점수 분포를 조회합니다 (인증 불필요)")
    fun getReviewScoreDistribution(
        @RequestParam("menu_id") menuId: Int,
    ): ReviewScoreDistributionResponse {
        return reviewService.getScoreDistribution(menuId)
    }

    @GetMapping("/keyword/dist")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "키워드 점수 분포 조회", description = "메뉴의 키워드 기반 점수 분포를 조회합니다 (인증 불필요)")
    fun getKeywordScoreDistribution(
        @RequestParam("menu_id") menuId: Int,
    ): KeywordScoreDistributionResponse {
        return reviewService.getKeywordScoreDistribution(menuId)
    }

    // ============================================================================
    // GET Endpoints (Conditional Authentication via is_private parameter)
    // ============================================================================

    @GetMapping("/{review_id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "리뷰 상세 조회",
        description = "특정 리뷰의 상세 정보를 조회합니다. 'is_login' 파라미터에 따라 인증이 조건부로 적용됩니다.",
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getReview(
        @PathVariable("review_id") reviewId: Int,
        @Parameter(description = "사용자 로그인 여부. 인증 요구사항을 결정합니다.")
        @RequestParam("is_login") isLogin: Boolean,
        request: HttpServletRequest,
    ): MyReviewResponse {
        val userId = request.userId
        return reviewService.getReview(reviewId = reviewId, userId = userId)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "메뉴의 리뷰 목록 조회",
        description = "특정 메뉴의 리뷰 목록을 조회합니다. 'is_login' 파라미터에 따라 인증이 조건부로 적용됩니다.",
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getReviews(
        @RequestParam("menu_id") menuId: Int,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @Parameter(description = "사용자 로그인 여부. 인증 요구사항을 결정합니다.")
        @RequestParam("is_login") isLogin: Boolean,
        request: HttpServletRequest,
    ): ReviewListResponse {
        val userId = request.userId
        return reviewService.getReviews(userId, menuId, page, size)
    }

    @GetMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "메뉴의 필터링된 리뷰 조회",
        description = "특정 메뉴의 리뷰를 댓글/이미지 필터와 함께 조회합니다. 'is_login' 파라미터에 따라 인증이 조건부로 적용됩니다.",
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getFilteredReviews(
        @RequestParam("menu_id") menuId: Int,
        @RequestParam("comment", required = false) comment: Boolean?,
        @RequestParam("image", required = false) image: Boolean?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @Parameter(description = "사용자 로그인 여부. 인증 요구사항을 결정합니다.")
        @RequestParam("is_login") isLogin: Boolean,
        request: HttpServletRequest,
    ): ReviewListResponse {
        val userId = request.userId
        return reviewService.getFilteredReviews(userId, menuId, comment, image, page, size)
    }
}
