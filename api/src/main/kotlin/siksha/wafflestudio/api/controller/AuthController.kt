package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.auth.dto.AuthResponseDto
import siksha.wafflestudio.core.domain.auth.service.AuthService
import siksha.wafflestudio.core.domain.user.dto.UserProfilePatchDto
import siksha.wafflestudio.core.domain.user.dto.UserResponseDto
import siksha.wafflestudio.core.domain.user.service.UserService

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
    private val resourceLoader: ResourceLoader,
) {
    @PostMapping("/refresh")
    fun refreshAccessToken(request: HttpServletRequest): AuthResponseDto {
        return authService.getAccessTokenByUserId(request.userId)
    }

    @GetMapping("/privacy-policy", produces = [MediaType.TEXT_HTML_VALUE])
    fun getPrivacyPolicy(): ResponseEntity<Resource> {
        val resource = resourceLoader.getResource("classpath:static/privacy_policy.html")
        return ResponseEntity.ok(resource)
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(request: HttpServletRequest) {
        userService.deleteUser(request.userId)
    }
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

    @GetMapping("/me")
    fun getMyInfo(request: HttpServletRequest): UserResponseDto {
        return userService.getUser(request.userId)
    }

    // TODO: request param 재정의
    @PatchMapping("/me/profile", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateUserProfile(
        request: HttpServletRequest,
        @RequestPart("nickname") nickname: String?,
        @RequestPart("image") image: MultipartFile?,
        @RequestPart("change_to_default_image") changeToDefaultImage: Boolean? = false,
    ): UserResponseDto {
        val patchDto =
            UserProfilePatchDto(
                nickname = nickname,
                image = image,
                changeToDefaultImage = changeToDefaultImage ?: false,
            )

        return userService.patchUser(request.userId, patchDto)
    }


    @GetMapping("/nicknames/validate")
    @ResponseStatus(HttpStatus.OK)
    fun validateNickname(
        @RequestParam("nickname") nickname: String,
    ) {
        userService.validateNickname(nickname)
    }
}
