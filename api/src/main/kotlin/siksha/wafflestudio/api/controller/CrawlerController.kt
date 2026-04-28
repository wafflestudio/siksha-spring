package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.core.domain.main.meal.dto.CrawlerMealRequestDto
import siksha.wafflestudio.core.domain.main.meal.usecase.SyncMealUseCase

@RestController
@Tag(name = "Crawler", description = "크롤러 데이터 수집 엔드포인트")
class CrawlerController(
    private val syncMealUseCase: SyncMealUseCase,
) {
    // POST /crawler/meals
    @PostMapping("/crawler/meals")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "식단 동기화", description = "크롤러에서 수집한 식단 데이터를 V2 테이블에 동기화합니다.")
    fun syncMeals(
        @RequestBody request: CrawlerMealRequestDto,
    ) {
        syncMealUseCase(request)
    }
}
