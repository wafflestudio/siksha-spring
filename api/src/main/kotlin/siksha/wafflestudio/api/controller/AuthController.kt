package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.api.common.userId
import siksha.wafflestudio.core.domain.auth.dto.AuthResponseDto
import siksha.wafflestudio.core.domain.auth.dto.LoginTypeTestRequestDto
import siksha.wafflestudio.core.domain.auth.service.AuthService
import siksha.wafflestudio.core.domain.auth.social.SocialTokenVerifier
import siksha.wafflestudio.core.domain.auth.social.data.SocialProfile
import siksha.wafflestudio.core.domain.auth.social.data.SocialProvider
import siksha.wafflestudio.core.domain.common.exception.TokenParseException
import siksha.wafflestudio.core.domain.user.dto.AlarmSettingRequestDto
import siksha.wafflestudio.core.domain.user.dto.UserAlarmResponseDto
import siksha.wafflestudio.core.domain.user.dto.UserDeviceDto
import siksha.wafflestudio.core.domain.user.dto.UserProfilePatchDto
import siksha.wafflestudio.core.domain.user.dto.UserResponseDto
import siksha.wafflestudio.core.domain.user.service.UserService

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증 및 사용자 관리 엔드포인트")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
    private val resourceLoader: ResourceLoader,
    private val verifier: SocialTokenVerifier,
) {
    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 재발급", description = "인증된 사용자의 액세스 토큰을 재발급합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun refreshAccessToken(request: HttpServletRequest): AuthResponseDto {
        return authService.getAccessTokenByUserId(request.userId)
    }

    @GetMapping("/privacy-policy", produces = [MediaType.TEXT_HTML_VALUE])
    @Operation(summary = "개인정보 처리방침 조회", description = "서비스의 개인정보 처리방침을 HTML 형식으로 조회합니다")
    fun getPrivacyPolicy(): ResponseEntity<Resource> {
        val resource = resourceLoader.getResource("classpath:static/privacy_policy.html")
        return ResponseEntity.ok(resource)
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "회원 탈퇴", description = "인증된 사용자의 계정을 삭제합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun deleteUser(request: HttpServletRequest) {
        userService.deleteUser(request.userId)
    }

    @PostMapping("/login/test")
    @Operation(summary = "테스트 로그인", description = "테스트 환경에서 사용하는 로그인입니다")
    fun loginTypeTest(
        @RequestBody body: LoginTypeTestRequestDto,
    ): AuthResponseDto {
        return authService.getOrCreateAccessTokenBySocialProfile(
            SocialProfile(provider = SocialProvider.TEST, externalId = body.identity),
        )
    }

    @PostMapping("/login/apple")
    @Operation(summary = "Apple 로그인", description = "Apple 소셜 로그인을 통해 인증합니다")
    fun loginTypeApple(request: HttpServletRequest): AuthResponseDto {
        val token = trimTokenHeader(request, SocialProvider.APPLE)
        val socialProfile = verifier.verifyAppleIdToken(token)
        return authService.getOrCreateAccessTokenBySocialProfile(socialProfile)
    }

    @PostMapping("/login/google")
    @Operation(summary = "Google 로그인", description = "Google 소셜 로그인을 통해 인증합니다")
    fun loginTypeGoogle(request: HttpServletRequest): AuthResponseDto {
        val token = trimTokenHeader(request, SocialProvider.GOOGLE)
        val socialProfile = verifier.verifyGoogleIdToken(token)
        return authService.getOrCreateAccessTokenBySocialProfile(socialProfile)
    }

    @PostMapping("/login/kakao")
    @Operation(summary = "Kakao 로그인", description = "Kakao 소셜 로그인을 통해 인증합니다")
    fun loginTypeKakao(request: HttpServletRequest): AuthResponseDto {
        val token = trimTokenHeader(request, SocialProvider.KAKAO)
        val socialProfile = verifier.verifyKakaoAccessToken(token)
        return authService.getOrCreateAccessTokenBySocialProfile(socialProfile)
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "인증된 사용자의 프로필 정보를 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun getMyInfo(request: HttpServletRequest): UserResponseDto {
        return userService.getUser(request.userId)
    }

    @PatchMapping("/me/profile", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "내 프로필 수정", description = "인증된 사용자의 프로필 정보를 수정합니다")
    @SecurityRequirement(name = "bearerAuth")
    fun updateUserProfile(
        request: HttpServletRequest,
        @ModelAttribute userProfilePatchDto: UserProfilePatchDto,
    ): UserResponseDto {
        return userService.patchUser(request.userId, userProfilePatchDto)
    }

    @GetMapping("/nicknames/validate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "닉네임 유효성 검증", description = "닉네임의 중복 여부 및 유효성을 검증합니다")
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

    @PostMapping("/userDevice")
    fun createUserDevice(
        request: HttpServletRequest,
        @RequestBody userDeviceDto: UserDeviceDto,
    ) {
        userService.createUserDevice(
            userId = request.userId,
            fcmToken = userDeviceDto.fcmToken,
        )
    }

    @DeleteMapping("/userDevice")
    fun deleteUserDevice(
        request: HttpServletRequest,
        @RequestBody userDeviceDeleteDto: UserDeviceDto,
    ) {
        userService.deleteUserDevice(
            userId = request.userId,
            fcmToken = userDeviceDeleteDto.fcmToken,
        )
    }

    @PostMapping("/alarm")
    fun setAlarm(
        request: HttpServletRequest,
        @RequestBody alarmSettingRequest: AlarmSettingRequestDto,
    ) {
        userService.setAlarm(
            userId = request.userId,
            alarmType = alarmSettingRequest.type,
        )
    }

    @GetMapping("/alarm")
    fun getAlarm(request: HttpServletRequest): UserAlarmResponseDto {
        return userService.getAlarm(request.userId)
    }

    companion object {
        private const val APPLE_AUTHORIZATION_HEADER_NAME = "apple-token"
        private const val GOOGLE_AUTHORIZATION_HEADER_NAME = "google-token"
        private const val KAKAO_AUTHORIZATION_HEADER_NAME = "kakao-token"
    }
}
