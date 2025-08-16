package siksha.wafflestudio.core.domain.auth.social

interface SocialTokenVerifier {
    fun verify(token: String): String // Social ID 반환
}
