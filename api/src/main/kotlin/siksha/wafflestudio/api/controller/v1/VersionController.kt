package siksha.wafflestudio.api.controller.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.core.domain.v1.version.data.ClientType
import siksha.wafflestudio.core.domain.v1.version.dto.VersionResponseDto
import siksha.wafflestudio.core.domain.v1.version.dto.VersionUpdateRequestDto
import siksha.wafflestudio.core.domain.v1.version.service.AppVersionService

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

    @PatchMapping("/versions/{clientType}")
    @Operation(summary = "클라이언트별 최소 앱 버전 수정", description = "최소 허용 버전을 수정합니다.")
    @SecurityRequirement(name = "crawlerApiKey")
    fun updateVersion(
        @PathVariable clientType: String,
        @Valid @RequestBody request: VersionUpdateRequestDto,
    ): VersionResponseDto =
        appVersionService.updateMinimumVersion(
            clientType = ClientType.from(clientType),
            minimumVersion = request.minimumVersion,
        )
}
