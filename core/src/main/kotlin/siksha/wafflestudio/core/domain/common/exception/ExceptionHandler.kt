package siksha.wafflestudio.core.domain.common.exception

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(SikshaException::class)
    fun handleSikshaException(sikshaException: SikshaException): ResponseEntity<ErrorBody> {
        return ResponseEntity(
            ErrorBody(sikshaException.errorMessage),
            sikshaException.httpStatus,
        )
    }

    // pagination; pydantic에서 validation 실패 시 422를 내려주므로, 동일하게 처리
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(constraintViolationException: ConstraintViolationException): ResponseEntity<ErrorBody> {
        return ResponseEntity(
            constraintViolationException.message?.let { ErrorBody(it) },
            HttpStatus.UNPROCESSABLE_ENTITY,
        )
    }
}
