package siksha.wafflestudio.core.service.version

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.common.exception.VersionNotFoundException
import siksha.wafflestudio.core.domain.version.data.AppVersion
import siksha.wafflestudio.core.domain.version.data.ClientType
import siksha.wafflestudio.core.domain.version.repository.AppVersionRepository
import siksha.wafflestudio.core.domain.version.service.AppVersionService

class AppVersionServiceTest {
    private lateinit var appVersionRepository: AppVersionRepository
    private lateinit var service: AppVersionService

    @BeforeEach
    internal fun setUp() {
        appVersionRepository = mockk()
        service = AppVersionService(appVersionRepository)
        clearAllMocks()
    }

    @Test
    fun `get minimum version by client type`() {
        every { appVersionRepository.findByClientType(ClientType.AND) } returns
            AppVersion(
                id = 1,
                minimumVersion = "2.1.0",
                clientType = ClientType.AND,
            )

        val result = service.getMinimumVersion(ClientType.AND)

        assertEquals("2.1.0", result.minimumVersion)
    }

    @Test
    fun `throw when version policy does not exist`() {
        every { appVersionRepository.findByClientType(ClientType.IOS) } returns null

        assertThrows(VersionNotFoundException::class.java) {
            service.getMinimumVersion(ClientType.IOS)
        }
    }
}
