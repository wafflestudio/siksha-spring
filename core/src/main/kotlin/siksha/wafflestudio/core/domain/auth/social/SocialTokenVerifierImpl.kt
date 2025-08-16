package siksha.wafflestudio.core.domain.auth.social

class SocialTokenVerifierImpl(

): SocialTokenVerifier {
    override fun verifyGoogleIdToken(idToken: String, clientId: String): SocialProfile {
        TODO("Not yet implemented")
    }

    override fun verifyAppleIdToken(idToken: String, clientId: String): SocialProfile {
        TODO("Not yet implemented")
    }

    override fun verifyKakaoIdToken(idToken: String, clientId: String): SocialProfile {
        TODO("Not yet implemented")
    }

    override fun verifyKakaoAccessToken(accessToken: String): SocialProfile {
        TODO("Not yet implemented")
    }
}
