package siksha.wafflestudio.core.domain.common.exception.main

import org.springframework.http.HttpStatus

class InvalidScoreException : MainException(HttpStatus.BAD_REQUEST, "평점은 1에서 5 사이여야 합니다.")

class KeywordMissingException : MainException(HttpStatus.BAD_REQUEST, "작성하지 않은 키워드 리뷰가 존재합니다.")

class ReviewAlreadyExistsException : MainException(HttpStatus.CONFLICT, "이 메뉴에 대한 리뷰가 이미 존재합니다")

class ReviewSaveFailedException : MainException(HttpStatus.INTERNAL_SERVER_ERROR, "리뷰 저장 중에 오류가 발생했습니다.")

class SelfReviewLikeNotAllowedException : MainException(HttpStatus.BAD_REQUEST, "본인의 리뷰에는 좋아요를 누를 수 없습니다.")

class ReviewNotFoundException : MainException(HttpStatus.NOT_FOUND, "해당 리뷰가 존재하지 않습니다.")

class NotReviewOwnerException : MainException(HttpStatus.NOT_FOUND, "해당 리뷰를 작성한 사용자가 아닙니다.")

class KeywordReviewNotFoundException : MainException(HttpStatus.NOT_FOUND, "해당 키워드 리뷰가 존재하지 않습니다.")

class ReviewAndMenuMismatchException : MainException(HttpStatus.BAD_REQUEST, "해당 리뷰의 메뉴와 요청하신 메뉴가 일치하지 않아 리뷰를 수정할 수 없습니다.")
