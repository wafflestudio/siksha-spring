package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.core.domain.version.data.ClientType
import siksha.wafflestudio.core.domain.version.dto.VersionResponseDto
import siksha.wafflestudio.core.domain.version.service.AppVersionService

@RestController
@Tag(name = "Versions", description = "앱 최소 버전 정책 조회 엔드포인트")
class VersionController(
    private val appVersionService: AppVersionService,
) {
    @GetMapping("/versions/{clientType}")
    @Operation(summary = "클라이언트별 최소 앱 버전 조회", description = "최소 허용 버전을 조회합니다")
    fun getVersion(
        @PathVariable clientType: String,
    ): VersionResponseDto = appVersionService.getMinimumVersion(ClientType.from(clientType))
}
