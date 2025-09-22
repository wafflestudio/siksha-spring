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
import siksha.wafflestudio.core.domain.auth.dto.AuthResponseDto
import siksha.wafflestudio.core.domain.auth.dto.LoginTypeTestRequestDto
import siksha.wafflestudio.core.domain.auth.service.AuthService
import siksha.wafflestudio.core.domain.auth.social.SocialTokenVerifier
import siksha.wafflestudio.core.domain.auth.social.data.SocialProfile
import siksha.wafflestudio.core.domain.auth.social.data.SocialProvider
import siksha.wafflestudio.core.domain.common.exception.TokenParseException
import siksha.wafflestudio.core.domain.user.dto.UserProfilePatchDto
import siksha.wafflestudio.core.domain.user.dto.UserResponseDto
import siksha.wafflestudio.core.domain.user.service.UserService

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
    private val resourceLoader: ResourceLoader,
    private val verifier: SocialTokenVerifier,
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

    @PostMapping("/login/test")
    fun loginTypeTest(
        @RequestBody body: LoginTypeTestRequestDto,
    ): AuthResponseDto {
        return authService.getOrCreateAccessTokenBySocialProfile(
            SocialProfile(provider = SocialProvider.TEST, externalId = body.identity),
        )
    }

    @PostMapping("/login/{provider}")
    fun login(
        @PathVariable("provider") provider: SocialProvider,
        request: HttpServletRequest,
    ): AuthResponseDto {
        val token = trimTokenHeader(request, provider)
        val socialProfile =
            when (provider) {
                SocialProvider.APPLE -> verifier.verifyAppleIdToken(token)
                SocialProvider.GOOGLE -> verifier.verifyGoogleIdToken(token)
                SocialProvider.KAKAO -> verifier.verifyKakaoAccessToken(token)
                SocialProvider.TEST -> error("unreachable") // loginTypeTest() will handle this
            }
        return authService.getOrCreateAccessTokenBySocialProfile(socialProfile)
    }

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

    private fun trimTokenHeader(
        request: HttpServletRequest,
        provider: SocialProvider,
    ): String {
        val rawHeader =
            request.getHeader("Authorization")
                ?: when (provider) {
                    // legacy headers
                    // TODO: deprecate below
                    SocialProvider.APPLE -> request.getHeader(APPLE_AUTHORIZATION_HEADER_NAME)
                    SocialProvider.GOOGLE -> request.getHeader(GOOGLE_AUTHORIZATION_HEADER_NAME)
                    SocialProvider.KAKAO -> request.getHeader(KAKAO_AUTHORIZATION_HEADER_NAME)
                    SocialProvider.TEST -> throw TokenParseException()
                }

        if (!rawHeader.startsWith("Bearer ", ignoreCase = false)) throw TokenParseException()
        return rawHeader.removePrefix("Bearer ")
    }

    companion object {
        private const val APPLE_AUTHORIZATION_HEADER_NAME = "apple-token"
        private const val GOOGLE_AUTHORIZATION_HEADER_NAME = "google-token"
        private const val KAKAO_AUTHORIZATION_HEADER_NAME = "kakao-token"
    }
}
