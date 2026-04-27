package siksha.wafflestudio.core.domain.version.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.version.data.AppVersion
import siksha.wafflestudio.core.domain.version.data.ClientType

@Repository
interface AppVersionRepository : JpaRepository<AppVersion, Int> {
    fun findByClientType(clientType: ClientType): AppVersion?
}
