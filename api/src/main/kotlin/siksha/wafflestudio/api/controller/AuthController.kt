package siksha.wafflestudio.api.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.auth.JwtProvider
import siksha.wafflestudio.core.domain.auth.dto.AuthResponseDto
import siksha.wafflestudio.core.domain.auth.service.AuthService
import siksha.wafflestudio.core.domain.auth.social.SocialProfile
import siksha.wafflestudio.core.domain.auth.social.SocialTokenVerifier
import siksha.wafflestudio.core.domain.user.dto.UserProfilePatchDto
import siksha.wafflestudio.core.domain.user.dto.UserResponseDto
import siksha.wafflestudio.core.domain.user.dto.UserWithProfileUrlResponseDto
import siksha.wafflestudio.core.domain.user.service.UserService

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
    private val resourceLoader: ResourceLoader,
    @Value("\${siksha.oauth.google.client-id.web}") private val googleClientId: String,
    @Value("\${siksha.oauth.apple.approved-audience}")  private val appleClientId: String,
    @Value("\${siksha.oauth.kakao.app-id}")  private val kakaoClientId: String,
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
//
//    @PostMapping("/login/test")
//    fun loginTypeTest(){}
//
    data class TokenReq(val token: String)
    @PostMapping("/login/apple")
    fun loginTypeApple(@RequestBody req: TokenReq) = issue(verifier.verifyAppleIdToken(req.token, appleClientId))


    @PostMapping("/login/kakao")
    fun loginTypeKakao(@RequestBody req: TokenReq): AuthResponseDto {
        val isJwt = req.token.count { it == '.' } == 2
        val profile = if (isJwt) verifier.verifyKakaoIdToken(req.token, kakaoClientId)
        else      verifier.verifyKakaoAccessToken(req.token)
        return issue(profile)
    }

    @PostMapping("/login/google")
    fun loginTypeGoogle(@RequestBody req: TokenReq) = issue(verifier.verifyGoogleIdToken(req.token, googleClientId))

    private fun issue(p: SocialProfile): AuthResponseDto {
        val userId = authService.upsertAndGetUserId(p)   // (provider, externalId=sub) → userId 매핑
        val access = authService.getAccessTokenByUserId(userId)
        return access
    }
    // TODO: deprecate this
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

    @GetMapping("/me/image")
    fun getMyInfoWithProfileUrl(request: HttpServletRequest): UserWithProfileUrlResponseDto {
        return userService.getUserWithProfileUrl(request.userId)
    }

    @PatchMapping("/me/image/profile", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateUserProfileWithProfileUrl(
        request: HttpServletRequest,
        @RequestPart("nickname") nickname: String?,
        @RequestPart("image") image: MultipartFile?,
        @RequestPart("change_to_default_image") changeToDefaultImage: Boolean? = false,
    ): UserWithProfileUrlResponseDto {
        val patchDto =
            UserProfilePatchDto(
                nickname = nickname,
                image = image,
                changeToDefaultImage = changeToDefaultImage ?: false,
            )

        return userService.patchUserWithProfileUrl(request.userId, patchDto)
    }

    @GetMapping("/nicknames/validate")
    @ResponseStatus(HttpStatus.OK)
    fun validateNickname(
        @RequestParam("nickname") nickname: String,
    ) {
        userService.validateNickname(nickname)
    }
}
