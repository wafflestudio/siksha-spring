package siksha.wafflestudio.core.domain.auth.social

enum class SocialProvider {
    KAKAO,
    GOOGLE,
    APPLE,
    TEST;
}
data class SocialProfile(
    val provider: SocialProvider,
    val externalId: String,
)
