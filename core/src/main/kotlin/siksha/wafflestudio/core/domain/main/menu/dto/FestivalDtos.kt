package siksha.wafflestudio.core.domain.main.menu.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDate

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class FestivalDatesResponseDto
    @JsonCreator
    constructor(
        @JsonProperty("festival_dates")
        val festivalDates: List<LocalDate>,
    )

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class IsFestivalResponseDto
    @JsonCreator
    constructor(
        @JsonProperty("target_date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        val targetDate: LocalDate,
        @JsonProperty("is_festival")
        val isFestival: Boolean,
    )
