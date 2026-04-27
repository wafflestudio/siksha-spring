package siksha.wafflestudio.core.domain.version.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.common.exception.VersionNotFoundException
import siksha.wafflestudio.core.domain.version.data.ClientType
import siksha.wafflestudio.core.domain.version.dto.VersionResponseDto
import siksha.wafflestudio.core.domain.version.repository.AppVersionRepository

@Service
class AppVersionService(
    private val appVersionRepository: AppVersionRepository,
) {
    @Cacheable(value = ["appVersionCache"], key = "#clientType.name()")
    fun getMinimumVersion(clientType: ClientType): VersionResponseDto {
        val appVersion =
            appVersionRepository.findByClientType(clientType)
                ?: throw VersionNotFoundException(clientType.name)

        return VersionResponseDto.from(appVersion)
    }
}
