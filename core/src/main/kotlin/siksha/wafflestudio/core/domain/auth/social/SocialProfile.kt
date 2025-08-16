package siksha.wafflestudio.core.domain.auth.social

data class SocialProfile(
    val provider: String,
    val externalId: String,
)
