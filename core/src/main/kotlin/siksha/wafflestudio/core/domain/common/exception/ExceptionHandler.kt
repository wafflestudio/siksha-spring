package siksha.wafflestudio.core.domain.common.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import siksha.wafflestudio.core.domain.common.exception.auth.UnauthorizedUserException
import siksha.wafflestudio.core.domain.common.exception.user.UserNotFoundException

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

    // auth 관련 api에서, JWT payload로부터 얻은 userId를 사용하는 경우
    // 보안성 및 일관성을 위해 일괄 401로 응답
    @ExceptionHandler(UserNotFoundException::class)
    fun handleAuthUserNotFoundException(
        request: HttpServletRequest,
        userNotFoundException: UserNotFoundException,
    ): ResponseEntity<ErrorBody> {
        if (request.requestURI.contains("/auth")) {
            return ResponseEntity(
                UnauthorizedUserException().message?.let { ErrorBody(it) },
                HttpStatus.UNAUTHORIZED,
            )
        } else {
            return ResponseEntity(
                ErrorBody(userNotFoundException.errorMessage),
                userNotFoundException.httpStatus,
            )
        }
    }
}
