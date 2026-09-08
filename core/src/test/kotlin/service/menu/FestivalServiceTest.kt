package siksha.wafflestudio.core.service.menu

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.v1.main.menu.service.FestivalService
import java.time.LocalDate

class FestivalServiceTest {
    private lateinit var service: FestivalService

    @BeforeEach
    internal fun setUp() {
        service = FestivalService()
    }

    @Test
    fun `return all festival dates`() {
        val result = service.getFestival()

        assertEquals(
            listOf(
                LocalDate.of(2026, 9, 15),
                LocalDate.of(2026, 9, 16),
                LocalDate.of(2026, 9, 17),
            ),
            result.festivalDates,
        )
    }

    @Test
    fun `return true when input date is festival`() {
        val result = service.getIsFestivalWhereDate(LocalDate.of(2026, 9, 15))

        assertEquals(LocalDate.of(2026, 9, 15), result.targetDate)
        assertTrue(result.isFestival)
    }

    @Test
    fun `return false when input date is not festival`() {
        val result = service.getIsFestivalWhereDate(LocalDate.of(2026, 9, 14))

        assertEquals(LocalDate.of(2026, 9, 14), result.targetDate)
        assertFalse(result.isFestival)
    }
}
