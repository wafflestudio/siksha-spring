package siksha.wafflestudio.core.service.version

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.common.exception.version.VersionNotFoundException
import siksha.wafflestudio.core.domain.v1.version.data.AppVersion
import siksha.wafflestudio.core.domain.v1.version.data.ClientType
import siksha.wafflestudio.core.domain.v1.version.repository.AppVersionRepository
import siksha.wafflestudio.core.domain.v1.version.service.AppVersionService

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

    @Test
    fun `update minimum version by client type`() {
        val current =
            AppVersion(
                id = 2,
                minimumVersion = "3.5.0",
                clientType = ClientType.IOS,
            )
        every { appVersionRepository.findByClientType(ClientType.IOS) } returns current
        every { appVersionRepository.save(any()) } answers { firstArg() }

        val result = service.updateMinimumVersion(ClientType.IOS, "3.5.1")

        assertEquals("3.5.1", result.minimumVersion)
        verify {
            appVersionRepository.save(
                match {
                    it.id == current.id &&
                        it.clientType == ClientType.IOS &&
                        it.minimumVersion == "3.5.1"
                },
            )
        }
    }
}
