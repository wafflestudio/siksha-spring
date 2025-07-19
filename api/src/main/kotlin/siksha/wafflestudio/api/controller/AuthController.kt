package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.auth.dto.AuthResponseDto
import siksha.wafflestudio.core.domain.auth.service.AuthService
import siksha.wafflestudio.core.domain.user.dto.UserWithProfileUrlResponseDto
import siksha.wafflestudio.core.domain.user.dto.UserResponseDto
import siksha.wafflestudio.core.domain.user.service.UserService

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
) {
    @PostMapping("/refresh")
    fun refreshAccessToken(
        request: HttpServletRequest
    ): AuthResponseDto {
        return authService.getAccessTokenByUserId(request.userId)
    }
//
//    @GetMapping("/privacy-policy")
//    fun getPrivacyPolicy() {}
//
//    @DeleteMapping
//    fun deleteUser() {}
//
//    @PostMapping("/login/test")
//    fun loginTypeTest(){}
//
//    @PostMapping("/login/apple")
//    fun loginTypeApple(){}
//
//    @PostMapping("/login/kakao")
//    fun loginTypeKakao(){}
//
//    @PostMapping("/login/google")
//    fun loginTypeGoogle(){}

    // TODO: deprecate this
    @GetMapping("/me")
    fun getUserInfo(
        request: HttpServletRequest
    ): UserResponseDto {
        return userService.getUserInfo(request.userId)
    }

//    @PatchMapping("/me/profile")
//    fun updateUserProfile() {}

    @GetMapping("/me/image")
    fun getUserInfoWithProfileUrl(request: HttpServletRequest
    ): UserWithProfileUrlResponseDto {
        return userService.getUserInfoWithProfileUrl(request.userId)
    }

}
