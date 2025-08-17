package siksha.wafflestudio.core.domain.auth.social

import siksha.wafflestudio.core.domain.auth.social.SocialProfile

interface SocialTokenVerifier {
    fun verifyGoogleIdToken(idToken: String, clientIds: List<String>): SocialProfile
    fun verifyAppleIdToken(idToken: String, clientIds: List<String>): SocialProfile
    fun verifyKakaoIdToken(idToken: String, clientId: String): SocialProfile
    fun verifyKakaoAccessToken(accessToken: String): SocialProfile
}
