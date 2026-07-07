package siksha.wafflestudio.core.usecase.meal

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.main.meal.usecase.NormalizeMenuUseCase
import siksha.wafflestudio.core.domain.main.menu.data.MenuAliasV2
import siksha.wafflestudio.core.domain.main.menu.data.MenuV2
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAliasV2Repository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import kotlin.test.assertEquals

class NormalizeMenuUseCaseTest {
    private lateinit var menuAliasV2Repository: MenuAliasV2Repository
    private lateinit var menuV2Repository: MenuV2Repository
    private lateinit var useCase: NormalizeMenuUseCase

    @BeforeEach
    internal fun setUp() {
        menuAliasV2Repository = mockk()
        menuV2Repository = mockk()
        useCase = NormalizeMenuUseCase(menuAliasV2Repository, menuV2Repository)
        clearAllMocks()
    }

    @Test
    fun `alias exact hit이면 alias menu name으로 menu 조회 후 반환`() {
        val restaurant = testRestaurant("자하연식당 3층")
        val alias = MenuAliasV2(id = 1, alias = "치즈 돈까스", menuName = "치즈돈까스")
        val menu = MenuV2(id = 10, restaurant = restaurant, name = "치즈돈까스")

        every { menuAliasV2Repository.findByAlias("치즈 돈까스") } returns alias
        every { menuV2Repository.findByRestaurantAndName(restaurant, "치즈돈까스") } returns menu

        val result = useCase("치즈 돈까스", restaurant)

        assertEquals(menu, result)
        verify(exactly = 1) { menuAliasV2Repository.findByAlias("치즈 돈까스") }
        verify(exactly = 1) { menuV2Repository.findByRestaurantAndName(restaurant, "치즈돈까스") }
        verify(exactly = 0) { menuAliasV2Repository.save(any()) }
        verify(exactly = 0) { menuV2Repository.save(any()) }
    }

    @Test
    fun `alias miss이면 정규화된 menu 이름으로 기존 menu를 조회하고 alias를 저장한다`() {
        val restaurant = testRestaurant("자하연식당 3층")
        val menu = MenuV2(id = 10, restaurant = restaurant, name = "치즈돈까스")
        val aliasSlot = slot<MenuAliasV2>()

        every { menuAliasV2Repository.findByAlias("추천 치즈 돈까스 (추천)") } returns null
        every { menuV2Repository.findByRestaurantAndName(restaurant, "치즈돈까스") } returns menu
        every { menuAliasV2Repository.save(capture(aliasSlot)) } answers { firstArg() }

        val result = useCase("추천 치즈 돈까스 (추천)", restaurant)

        assertEquals(menu, result)
        assertEquals("추천 치즈 돈까스 (추천)", aliasSlot.captured.alias)
        assertEquals("치즈돈까스", aliasSlot.captured.menuName)
        verify(exactly = 1) { menuAliasV2Repository.findByAlias("추천 치즈 돈까스 (추천)") }
        verify(exactly = 1) { menuV2Repository.findByRestaurantAndName(restaurant, "치즈돈까스") }
        verify(exactly = 0) { menuV2Repository.save(any()) }
    }

    @Test
    fun `기존 menu가 없으면 정규화된 이름으로 새 menu를 만들고 alias를 저장한다`() {
        val restaurant = testRestaurant("자하연식당 3층")
        val createdMenu = MenuV2(id = 10, restaurant = restaurant, name = "김치찌개")
        val menuSlot = slot<MenuV2>()
        val aliasSlot = slot<MenuAliasV2>()

        every { menuAliasV2Repository.findByAlias("HOT 김치찌개 / 밥 포함") } returns null
        every { menuV2Repository.findByRestaurantAndName(restaurant, "김치찌개") } returns null
        every { menuV2Repository.save(capture(menuSlot)) } returns createdMenu
        every { menuAliasV2Repository.save(capture(aliasSlot)) } answers { firstArg() }

        val result = useCase("HOT 김치찌개 / 밥 포함", restaurant)

        assertEquals(createdMenu, result)
        assertEquals("김치찌개", menuSlot.captured.name)
        assertEquals("HOT 김치찌개 / 밥 포함", aliasSlot.captured.alias)
        assertEquals("김치찌개", aliasSlot.captured.menuName)
        verify(exactly = 1) { menuAliasV2Repository.findByAlias("HOT 김치찌개 / 밥 포함") }
        verify(exactly = 1) { menuV2Repository.findByRestaurantAndName(restaurant, "김치찌개") }
    }

    private fun testRestaurant(name: String): RestaurantV2 =
        RestaurantV2(
            id = 1,
            building = BuildingV2(id = 1, number = "109", defaultOrder = 1),
            name = name,
            defaultOrder = 1,
        )
}
