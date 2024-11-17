package siksha.wafflestudio.core.domain.common.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(SikshaException::class)
    fun handleEduspaceException(sikshaException: SikshaException): ResponseEntity<ErrorBody> {
        return ResponseEntity(
            ErrorBody(sikshaException.errorMessage),
            sikshaException.httpStatus,
        )
    }
}
