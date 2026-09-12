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

    @Test
    fun `festival menu sizes follow ascending ids regardless of query order`() {
        val small = menu(first.id, "LU", 101, "닭강정 (소)")
        val medium = menu(first.id, "LU", 102, "닭강정 (중)")
        val large = menu(first.id, "LU", 103, "닭강정 (대)")
        listOf(listOf(large, small, medium), listOf(medium, large, small)).forEach { input ->
            stubMenus(input)

            val response = service.getMenusWhereDate(date, date, true, null)
            val menus =
                response.result
                    .single()
                    .LU
                    .single()
                    .menus

            assertEquals(listOf(101, 102, 103), menus.map { it.id })
            assertEquals(listOf("닭강정 (소)", "닭강정 (중)", "닭강정 (대)"), menus.map { it.nameKr })
            assertEquals(listOf(1000, 1000, 1000), menus.map { it.price })
        }
    }

    @Test
    fun `Sillichochi handmade skewer is first with its existing smallest menu id`() {
        stubMenus(
            listOf(
                menu(featured.id, "DN", 326389, "떡꼬치"),
                menu(featured.id, "DN", 326391, "꼬치모듬 플래터"),
                menu(featured.id, "DN", 326388, "김용범 수제꼬치"),
                menu(featured.id, "DN", 326390, "소시지 꼬치"),
            ),
        )

        val response = service.getMenusWhereDate(date, date, true, 1)
        val menus =
            response.result
                .single()
                .DN
                .single()
                .menus

        assertEquals(listOf(326388, 326389, 326390, 326391), menus.map { it.id })
        assertEquals(listOf("김용범 수제꼬치", "떡꼬치", "소시지 꼬치", "꼬치모듬 플래터"), menus.map { it.nameKr })
    }

    @Test
    fun `ordinary restaurant menus retain repository order`() {
        stubMenus(listOf(menu(last.id, "LU", 103), menu(last.id, "LU", 101), menu(last.id, "LU", 102)))

        val response = service.getMenusWhereDate(date, date, true, null)
        val menus =
            response.result
                .single()
                .LU
                .single()
                .menus

        assertEquals(listOf(103, 101, 102), menus.map { it.id })
    }

    private fun menu(
        restaurantId: Int,
        type: String,
        menuId: Int = restaurantId,
        name: String = "메뉴",
    ): MenuSummary =
        mockk {
            every { getId() } returns menuId
            every { getRestaurantId() } returns restaurantId
            every { getCode() } returns "menu-$restaurantId"
            every { getDate() } returns date
            every { getType() } returns type
            every { getNameKr() } returns name
            every { getNameEn() } returns null
            every { getPrice() } returns 1000
            every { getEtc() } returns "[]"
            every { getCreatedAt() } returns Timestamp.valueOf("2026-09-12 00:00:00")
            every { getUpdatedAt() } returns Timestamp.valueOf("2026-09-12 00:00:00")
            every { getScore() } returns null
            every { getReviewCnt() } returns 0
        }
}
