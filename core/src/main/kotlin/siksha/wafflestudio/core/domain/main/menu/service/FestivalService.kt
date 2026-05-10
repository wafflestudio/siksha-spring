package siksha.wafflestudio.core.domain.main.menu.service

import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.main.menu.dto.FestivalDatesResponseDto
import siksha.wafflestudio.core.domain.main.menu.dto.IsFestivalResponseDto
import java.time.LocalDate

@Service
class FestivalService {
    companion object {
        private val festivalDates: List<LocalDate> =
            listOf(
                LocalDate.of(2026, 5, 12),
                LocalDate.of(2026, 5, 13),
                LocalDate.of(2026, 5, 14),
            )
    }

    fun getFestival(): FestivalDatesResponseDto = FestivalDatesResponseDto(festivalDates = festivalDates)

    fun getIsFestivalWhereDate(inputDate: LocalDate): IsFestivalResponseDto =
        IsFestivalResponseDto(
            targetDate = inputDate,
            isFestival = inputDate in festivalDates,
        )
}
