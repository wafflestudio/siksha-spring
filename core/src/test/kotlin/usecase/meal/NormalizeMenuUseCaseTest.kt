package siksha.wafflestudio.core.usecase.meal

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.main.meal.usecase.MenuNameNormalizer
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
    private lateinit var menuNameNormalizer: MenuNameNormalizer
    private lateinit var useCase: NormalizeMenuUseCase

    @BeforeEach
    internal fun setUp() {
        menuAliasV2Repository = mockk()
        menuV2Repository = mockk()
        menuNameNormalizer = mockk(relaxUnitFun = true)
        useCase = NormalizeMenuUseCase(menuAliasV2Repository, menuV2Repository, menuNameNormalizer)
        clearAllMocks()
    }

    @Test
    fun `alias exact hit이면 alias menu name으로 menu 조회 후 반환하고 model normalizer를 호출하지 않는다`() {
        val restaurant = testRestaurant("자하연식당 3층")
        val alias = MenuAliasV2(id = 1, alias = "치즈 돈까스", menuName = "치즈돈까스")
        val menu = MenuV2(id = 10, restaurant = restaurant, name = "치즈돈까스")

        every { menuAliasV2Repository.findByAlias("치즈 돈까스") } returns alias
        every { menuV2Repository.findByRestaurantAndName(restaurant, "치즈돈까스") } returns menu

        val result = useCase("치즈 돈까스", restaurant)

        assertEquals(menu, result)
        verify(exactly = 1) { menuAliasV2Repository.findByAlias("치즈 돈까스") }
        verify(exactly = 1) { menuV2Repository.findByRestaurantAndName(restaurant, "치즈돈까스") }
        verify(exactly = 0) { menuNameNormalizer.normalize(any()) }
        verify(exactly = 0) { menuAliasV2Repository.save(any()) }
        verify(exactly = 0) { menuV2Repository.save(any()) }
    }

    @Test
    fun `alias exact hit이면 규칙 기반 정규화보다 alias menu name을 우선한다`() {
        val restaurant = testRestaurant("자하연식당 3층")
        val alias = MenuAliasV2(id = 1, alias = "치즈 돈까스", menuName = "수제치즈돈까스")
        val menu = MenuV2(id = 10, restaurant = restaurant, name = "수제치즈돈까스")

        every { menuAliasV2Repository.findByAlias("치즈 돈까스") } returns alias
        every { menuV2Repository.findByRestaurantAndName(restaurant, "수제치즈돈까스") } returns menu

        val result = useCase("치즈 돈까스", restaurant)

        assertEquals(menu, result)
        verify(exactly = 1) { menuAliasV2Repository.findByAlias("치즈 돈까스") }
        verify(exactly = 1) { menuV2Repository.findByRestaurantAndName(restaurant, "수제치즈돈까스") }
        verify(exactly = 0) { menuV2Repository.findByRestaurantAndName(restaurant, "치즈돈까스") }
        verify(exactly = 0) { menuNameNormalizer.normalize(any()) }
        verify(exactly = 0) { menuAliasV2Repository.save(any()) }
        verify(exactly = 0) { menuV2Repository.save(any()) }
    }

    @Test
    fun `규칙 기반 정규화 이름으로 기존 menu가 있으면 model normalizer를 호출하지 않고 원본 alias를 저장한다`() {
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
        verify(exactly = 0) { menuNameNormalizer.normalize(any()) }
        verify(exactly = 1) { menuV2Repository.findByRestaurantAndName(restaurant, "치즈돈까스") }
        verify(exactly = 1) { menuNameNormalizer.addAlias("추천 치즈 돈까스 (추천)", "치즈돈까스") }
        verify(exactly = 0) { menuV2Repository.save(any()) }
    }

    @Test
    fun `model confidence가 낮고 기존 menu가 없으면 규칙 기반 정규화 이름으로 새 menu를 만들고 원본 alias를 저장한다`() {
        val restaurant = testRestaurant("자하연식당 3층")
        val createdMenu = MenuV2(id = 10, restaurant = restaurant, name = "김치찌개")
        val menuSlot = slot<MenuV2>()
        val aliasSlot = slot<MenuAliasV2>()

        every { menuAliasV2Repository.findByAlias("HOT 김치찌개 / 밥 포함") } returns null
        every { menuNameNormalizer.normalize("김치찌개") } returns
            MenuNameNormalizer.NormalizationResult(
                normalizedName = "다른메뉴",
                confidence = 0.5,
            )
        every { menuV2Repository.findByRestaurantAndName(restaurant, "김치찌개") } returns null
        every { menuV2Repository.save(capture(menuSlot)) } returns createdMenu
        every { menuAliasV2Repository.save(capture(aliasSlot)) } answers { firstArg() }

        val result = useCase("HOT 김치찌개 / 밥 포함", restaurant)

        assertEquals(createdMenu, result)
        assertEquals("김치찌개", menuSlot.captured.name)
        assertEquals("HOT 김치찌개 / 밥 포함", aliasSlot.captured.alias)
        assertEquals("김치찌개", aliasSlot.captured.menuName)
        verify(exactly = 1) { menuAliasV2Repository.findByAlias("HOT 김치찌개 / 밥 포함") }
        verify(exactly = 1) { menuNameNormalizer.normalize("김치찌개") }
        verify(exactly = 1) { menuV2Repository.findByRestaurantAndName(restaurant, "김치찌개") }
        verify(exactly = 1) { menuNameNormalizer.addAlias("HOT 김치찌개 / 밥 포함", "김치찌개") }
    }

    @Test
    fun `model confidence가 threshold를 넘으면 model 정규화 이름으로 menu를 조회하고 원본 alias를 저장한다`() {
        val restaurant = testRestaurant("자하연식당 3층")
        val menu = MenuV2(id = 10, restaurant = restaurant, name = "치즈돈까스")
        val aliasSlot = slot<MenuAliasV2>()

        every { menuAliasV2Repository.findByAlias("추천 치즈 돈까스 플러스") } returns null
        every { menuNameNormalizer.normalize("치즈돈까스플러스") } returns
            MenuNameNormalizer.NormalizationResult(
                normalizedName = "치즈돈까스",
                confidence = 0.95,
            )
        every { menuV2Repository.findByRestaurantAndName(restaurant, "치즈돈까스플러스") } returns null
        every { menuV2Repository.findByRestaurantAndName(restaurant, "치즈돈까스") } returns menu
        every { menuAliasV2Repository.save(capture(aliasSlot)) } answers { firstArg() }

        val result = useCase("추천 치즈 돈까스 플러스", restaurant)

        assertEquals(menu, result)
        assertEquals("추천 치즈 돈까스 플러스", aliasSlot.captured.alias)
        assertEquals("치즈돈까스", aliasSlot.captured.menuName)
        verify(exactly = 1) { menuAliasV2Repository.findByAlias("추천 치즈 돈까스 플러스") }
        verify(exactly = 1) { menuV2Repository.findByRestaurantAndName(restaurant, "치즈돈까스플러스") }
        verify(exactly = 1) { menuNameNormalizer.normalize("치즈돈까스플러스") }
        verify(exactly = 1) { menuV2Repository.findByRestaurantAndName(restaurant, "치즈돈까스") }
        verify(exactly = 1) { menuNameNormalizer.addAlias("추천 치즈 돈까스 플러스", "치즈돈까스") }
        verify(exactly = 0) { menuV2Repository.save(any()) }
    }

    @Test
    fun `원본 이름 그대로 새 menu를 만들면 canonical 이름을 model index에 추가한다`() {
        val restaurant = testRestaurant("자하연식당 3층")
        val createdMenu = MenuV2(id = 10, restaurant = restaurant, name = "비빔밥")

        every { menuAliasV2Repository.findByAlias("비빔밥") } returns null
        every { menuV2Repository.findByRestaurantAndName(restaurant, "비빔밥") } returns null
        every { menuNameNormalizer.normalize("비빔밥") } returns
            MenuNameNormalizer.NormalizationResult(
                normalizedName = "다른메뉴",
                confidence = 0.5,
            )
        every { menuV2Repository.save(any()) } returns createdMenu

        val result = useCase("비빔밥", restaurant)

        assertEquals(createdMenu, result)
        verify(exactly = 1) { menuV2Repository.save(any()) }
        verify(exactly = 1) { menuNameNormalizer.addAlias("비빔밥", "비빔밥") }
        verify(exactly = 0) { menuAliasV2Repository.save(any()) }
    }

    private fun testRestaurant(name: String): RestaurantV2 =
        RestaurantV2(
            id = 1,
            building = BuildingV2(id = 1, number = "109", defaultOrder = 1),
            name = name,
            defaultOrder = 1,
        )
}
