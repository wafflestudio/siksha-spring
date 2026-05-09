package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.core.domain.main.menu.dto.FestivalDatesResponseDto
import siksha.wafflestudio.core.domain.main.menu.dto.IsFestivalResponseDto
import siksha.wafflestudio.core.domain.main.menu.service.FestivalService
import java.time.LocalDate

@RestController
@RequestMapping("/festival")
@Tag(name = "Festival", description = "축제 일정 조회 엔드포인트")
class FestivalController(
    private val festivalService: FestivalService,
) {
    // GET /festival/dates
    @GetMapping("/dates")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "축제 일정 조회", description = "축제 기간에 해당하는 날짜 목록을 조회합니다")
    fun getFestival(): FestivalDatesResponseDto = festivalService.getFestival()

    // GET /festival/{input_date}
    @GetMapping("/{input_date}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "축제 여부 조회", description = "특정 날짜가 축제 기간에 해당하는지 조회합니다")
    fun getIsFestivalWhereDate(
        @PathVariable("input_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) inputDate: LocalDate,
    ): IsFestivalResponseDto = festivalService.getIsFestivalWhereDate(inputDate)
}
