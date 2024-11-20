package siksha.wafflestudio.core.domain.common.exception

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
}
