package siksha.wafflestudio.core.domain.auth.social

import siksha.wafflestudio.core.domain.auth.social.data.SocialProfile

interface SocialTokenVerifier {
    fun verifyGoogleIdToken(idToken: String): SocialProfile
    fun verifyAppleIdToken(idToken: String): SocialProfile
    fun verifyKakaoAccessToken(accessToken: String): SocialProfile
}
