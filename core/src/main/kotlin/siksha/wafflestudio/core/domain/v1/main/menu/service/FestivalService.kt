package siksha.wafflestudio.core.domain.v1.main.menu.service

import org.springframework.stereotype.Service
import siksha.wafflestudio.core.domain.v1.main.menu.dto.FestivalDatesResponseDto
import siksha.wafflestudio.core.domain.v1.main.menu.dto.IsFestivalResponseDto
import java.time.LocalDate

@Service
class FestivalService {
    companion object {
        private val festivalDates: List<LocalDate> =
            listOf(
                LocalDate.of(2026, 9, 15),
                LocalDate.of(2026, 9, 16),
                LocalDate.of(2026, 9, 17),
            )
    }

    fun getFestival(): FestivalDatesResponseDto = FestivalDatesResponseDto(festivalDates = festivalDates)

    fun getIsFestivalWhereDate(inputDate: LocalDate): IsFestivalResponseDto =
        IsFestivalResponseDto(
            targetDate = inputDate,
            isFestival = inputDate in festivalDates,
        )
}
