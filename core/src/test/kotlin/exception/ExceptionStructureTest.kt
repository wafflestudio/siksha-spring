package exception

import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.common.exception.SikshaException
import siksha.wafflestudio.core.domain.common.exception.auth.AuthException
import siksha.wafflestudio.core.domain.common.exception.auth.UnauthorizedUserException
import siksha.wafflestudio.core.domain.common.exception.community.BoardNotFoundException
import siksha.wafflestudio.core.domain.common.exception.community.CommunityException
import siksha.wafflestudio.core.domain.common.exception.image.ImageException
import siksha.wafflestudio.core.domain.common.exception.image.ImageUploadFailedException
import siksha.wafflestudio.core.domain.common.exception.main.MainException
import siksha.wafflestudio.core.domain.common.exception.main.MenuNotFoundException
import siksha.wafflestudio.core.domain.common.exception.main.RestaurantException
import siksha.wafflestudio.core.domain.common.exception.main.RestaurantNotFoundException
import siksha.wafflestudio.core.domain.common.exception.user.UserException
import siksha.wafflestudio.core.domain.common.exception.user.UserNotFoundException
import siksha.wafflestudio.core.domain.common.exception.version.VersionException
import siksha.wafflestudio.core.domain.common.exception.version.VersionNotFoundException
import kotlin.test.assertIs

class ExceptionStructureTest {
    @Test
    fun `domain exceptions preserve the shared Siksha exception hierarchy`() {
        assertIs<SikshaException>(UnauthorizedUserException())
        assertIs<AuthException>(UnauthorizedUserException())
        assertIs<CommunityException>(BoardNotFoundException())
        assertIs<ImageException>(ImageUploadFailedException())
        assertIs<MainException>(MenuNotFoundException())
        assertIs<RestaurantException>(RestaurantNotFoundException())
        assertIs<UserException>(UserNotFoundException())
        assertIs<VersionException>(VersionNotFoundException("IOS"))
    }
}
