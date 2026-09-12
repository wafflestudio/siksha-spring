package service.menu

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.v1.main.menu.dto.MenuSummary
import siksha.wafflestudio.core.domain.v1.main.menu.repository.MenuRepository
import siksha.wafflestudio.core.domain.v1.main.menu.service.MenuService
import siksha.wafflestudio.core.domain.v1.main.restaurant.data.Restaurant
import siksha.wafflestudio.core.domain.v1.main.restaurant.repository.RestaurantRepository
import java.sql.Timestamp
import java.time.LocalDate

class FestivalMenuOrderTest {
    private val menuRepository = mockk<MenuRepository>()
    private val restaurantRepository = mockk<RestaurantRepository>()
    private val service = MenuService(menuRepository, restaurantRepository, mockk(), mockk())
    private val date = LocalDate.of(2026, 9, 15)
    private val first = Restaurant(1, "[축제]가게", "가게", null, null, null, null, null)
    private val featured = Restaurant(295, "[축제]실리꼬치밸리", "실리꼬치밸리", null, null, null, null, null)
    private val last = Restaurant(2, "학생회관", "학생회관", null, null, null, null, null)

    @Test
    fun `menu lists pin Sillichochi in every meal and preserve menus and remaining order`() {
        stubMenus(listOf(menu(first.id, "LU"), menu(featured.id, "LU"), menu(last.id, "LU")))

        val result = service.getMenusWhereDate(date, date, false, null)

        assertEquals(1, result.count)
        val day = result.result.single()
        listOf(day.BR, day.LU, day.DN).forEach { restaurants ->
            assertEquals(listOf(295, 1, 2), restaurants.map { it.id })
        }
        assertEquals(listOf(295, 1, 2), day.LU.flatMap { it.menus }.map { it.id })
    }

    @Test
    fun `except empty never resurrects Sillichochi when it has no menu`() {
        stubMenus(listOf(menu(first.id, "LU"), menu(last.id, "LU")))

        val result = service.getMenusWhereDate(date, date, true, null).result.single()

        assertEquals(listOf(1, 2), result.LU.map { it.id })
        assertEquals(emptyList<Int>(), result.BR.map { it.id })
        assertEquals(emptyList<Int>(), result.DN.map { it.id })
    }

    @Test
    fun `except empty keeps Sillichochi first when it has a menu`() {
        stubMenus(listOf(menu(first.id, "DN"), menu(featured.id, "DN")))

        val result = service.getMenusWhereDate(date, date, true, 1).result.single()

        assertEquals(listOf(295, 1), result.DN.map { it.id })
        assertEquals(emptyList<Int>(), result.LU.map { it.id })
    }

    private fun stubMenus(menus: List<MenuSummary>) {
        every { menuRepository.findMenusByDate(date.toString(), date.toString()) } returns menus
        every { menuRepository.findMenuLikesByDateAndUserId(any(), date.toString(), date.toString()) } returns emptyList()
        every { restaurantRepository.findAllByOrderByNameKr() } returns listOf(first, featured, last)
    }

    private fun menu(
        restaurantId: Int,
        type: String,
    ): MenuSummary =
        mockk {
            every { getId() } returns restaurantId
            every { getRestaurantId() } returns restaurantId
            every { getCode() } returns "menu-$restaurantId"
            every { getDate() } returns date
            every { getType() } returns type
            every { getNameKr() } returns "메뉴"
            every { getNameEn() } returns null
            every { getPrice() } returns 1000
            every { getEtc() } returns "[]"
            every { getCreatedAt() } returns Timestamp.valueOf("2026-09-12 00:00:00")
            every { getUpdatedAt() } returns Timestamp.valueOf("2026-09-12 00:00:00")
            every { getScore() } returns null
            every { getReviewCnt() } returns 0
        }
}
