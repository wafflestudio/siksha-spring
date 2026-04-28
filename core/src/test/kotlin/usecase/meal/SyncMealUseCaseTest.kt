package siksha.wafflestudio.core.usecase.meal

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFound
import siksha.wafflestudio.core.domain.main.meal.data.MealMenuV2
import siksha.wafflestudio.core.domain.main.meal.data.MealType
import siksha.wafflestudio.core.domain.main.meal.data.MealV2
import siksha.wafflestudio.core.domain.main.meal.dto.CrawlerMealRequestDto
import siksha.wafflestudio.core.domain.main.meal.repository.MealMenuV2Repository
import siksha.wafflestudio.core.domain.main.meal.repository.MealV2Repository
import siksha.wafflestudio.core.domain.main.meal.usecase.NormalizeMenuUseCase
import siksha.wafflestudio.core.domain.main.meal.usecase.SyncMealUseCase
import siksha.wafflestudio.core.domain.main.menu.data.MenuV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository
import java.time.LocalDate
import kotlin.test.assertEquals

class SyncMealUseCaseTest {
    private lateinit var restaurantV2Repository: RestaurantV2Repository
    private lateinit var mealV2Repository: MealV2Repository
    private lateinit var mealMenuV2Repository: MealMenuV2Repository
    private lateinit var normalizeMenuUseCase: NormalizeMenuUseCase
    private lateinit var useCase: SyncMealUseCase

    @BeforeEach
    internal fun setUp() {
        restaurantV2Repository = mockk()
        mealV2Repository = mockk()
        mealMenuV2Repository = mockk()
        normalizeMenuUseCase = mockk()
        useCase =
            SyncMealUseCase(
                restaurantV2Repository,
                mealV2Repository,
                mealMenuV2Repository,
                normalizeMenuUseCase,
            )
        clearAllMocks()
    }

    @Test
    fun `restaurant가 존재하지 않으면 RestaurantNotFound 던짐`() {
        // given
        every { restaurantV2Repository.findByName("없는식당") } returns null

        // when & then
        val request =
            CrawlerMealRequestDto(
                restaurant = "없는식당",
                date = LocalDate.of(2026, 4, 1),
                type = MealType.LUNCH,
                meals =
                    listOf(
                        CrawlerMealRequestDto.MealItem(
                            price = 1000,
                            noMeat = false,
                            menus = listOf("아무거나"),
                        ),
                    ),
            )
        assertThrows<RestaurantNotFound> { useCase(request) }

        // verify
        verify { restaurantV2Repository.findByName("없는식당") }
        verify(exactly = 0) { mealV2Repository.deleteAllByRestaurantAndDateAndType(any(), any(), any()) }
        verify(exactly = 0) { mealV2Repository.save(any()) }
    }

    @Test
    fun `정상 흐름 - 단일 meal과 단일 menu 동기화`() {
        // given
        val restaurant = RestaurantV2(id = 1, name = "자하연식당 3층")
        val date = LocalDate.of(2026, 4, 1)
        val type = MealType.LUNCH
        val savedMeal = MealV2(id = 100, restaurant = restaurant, date = date, type = type, price = 12000, noMeat = false)
        val normalizedMenu = MenuV2(id = 10, restaurant = restaurant, name = "뚝배기순두부")

        every { restaurantV2Repository.findByName("자하연식당 3층") } returns restaurant
        every { mealV2Repository.deleteAllByRestaurantAndDateAndType(restaurant, date, type) } just runs
        every { mealV2Repository.save(any()) } returns savedMeal
        every { normalizeMenuUseCase.invoke("뚝배기순두부", restaurant) } returns normalizedMenu
        every { mealMenuV2Repository.save(any()) } answers { firstArg() }

        // when
        val request =
            CrawlerMealRequestDto(
                restaurant = "자하연식당 3층",
                date = date,
                type = type,
                meals =
                    listOf(
                        CrawlerMealRequestDto.MealItem(
                            price = 12000,
                            noMeat = false,
                            menus = listOf("뚝배기순두부"),
                        ),
                    ),
            )
        useCase(request)

        // then
        verify { restaurantV2Repository.findByName("자하연식당 3층") }
        verify { mealV2Repository.deleteAllByRestaurantAndDateAndType(restaurant, date, type) }
        verify(exactly = 1) { mealV2Repository.save(any()) }
        verify(exactly = 1) { normalizeMenuUseCase.invoke("뚝배기순두부", restaurant) }
        verify(exactly = 1) { mealMenuV2Repository.save(any()) }
    }

    @Test
    fun `정상 흐름 - 여러 meal과 묶음 메뉴 동기화`() {
        // given
        val restaurant = RestaurantV2(id = 1, name = "자하연식당 3층")
        val date = LocalDate.of(2026, 4, 1)
        val type = MealType.LUNCH
        val savedMeal = MealV2(id = 100, restaurant = restaurant, date = date, type = type)
        val menuChicken = MenuV2(id = 1, restaurant = restaurant, name = "닭갈비")
        val menuSalad = MenuV2(id = 2, restaurant = restaurant, name = "그린샐러드")
        val menuSoup = MenuV2(id = 3, restaurant = restaurant, name = "열무된장국")

        every { restaurantV2Repository.findByName("자하연식당 3층") } returns restaurant
        every { mealV2Repository.deleteAllByRestaurantAndDateAndType(restaurant, date, type) } just runs
        every { mealV2Repository.save(any()) } returns savedMeal
        every { normalizeMenuUseCase.invoke("닭갈비", restaurant) } returns menuChicken
        every { normalizeMenuUseCase.invoke("그린샐러드", restaurant) } returns menuSalad
        every { normalizeMenuUseCase.invoke("열무된장국", restaurant) } returns menuSoup
        every { mealMenuV2Repository.save(any()) } answers { firstArg() }

        // when
        val request =
            CrawlerMealRequestDto(
                restaurant = "자하연식당 3층",
                date = date,
                type = type,
                meals =
                    listOf(
                        CrawlerMealRequestDto.MealItem(
                            price = 12000,
                            noMeat = false,
                            menus = listOf("닭갈비"),
                        ),
                        CrawlerMealRequestDto.MealItem(
                            price = 7000,
                            noMeat = false,
                            menus = listOf("그린샐러드", "열무된장국"),
                        ),
                    ),
            )
        useCase(request)

        // then
        verify(exactly = 2) { mealV2Repository.save(any()) }
        verify(exactly = 3) { normalizeMenuUseCase.invoke(any(), restaurant) }
        verify(exactly = 3) { mealMenuV2Repository.save(any()) }
    }

    @Test
    fun `meal_menu_v2에 original_name이 정확히 저장됨`() {
        // given
        val restaurant = RestaurantV2(id = 1, name = "자하연식당 3층")
        val date = LocalDate.of(2026, 4, 1)
        val type = MealType.LUNCH
        val savedMeal = MealV2(id = 100, restaurant = restaurant, date = date, type = type)
        val normalizedMenu = MenuV2(id = 10, restaurant = restaurant, name = "치즈돈까스")
        val mealMenuSlot = slot<MealMenuV2>()

        every { restaurantV2Repository.findByName(any()) } returns restaurant
        every { mealV2Repository.deleteAllByRestaurantAndDateAndType(any(), any(), any()) } just runs
        every { mealV2Repository.save(any()) } returns savedMeal
        every { normalizeMenuUseCase.invoke(any(), restaurant) } returns normalizedMenu
        every { mealMenuV2Repository.save(capture(mealMenuSlot)) } answers { firstArg() }

        // when
        val request =
            CrawlerMealRequestDto(
                restaurant = "자하연식당 3층",
                date = date,
                type = type,
                meals =
                    listOf(
                        CrawlerMealRequestDto.MealItem(
                            price = 5000,
                            noMeat = false,
                            menus = listOf("치즈 돈까스"),
                        ),
                    ),
            )
        useCase(request)

        // then
        assertEquals("치즈 돈까스", mealMenuSlot.captured.originalName)
        assertEquals("치즈돈까스", mealMenuSlot.captured.menu.name)
        assertEquals(savedMeal, mealMenuSlot.captured.meal)
    }

    @Test
    fun `meals가 빈 배열이면 delete만 수행하고 새로 저장하지 않음`() {
        // given
        val restaurant = RestaurantV2(id = 1, name = "자하연식당 3층")
        val date = LocalDate.of(2026, 4, 1)
        val type = MealType.LUNCH

        every { restaurantV2Repository.findByName("자하연식당 3층") } returns restaurant
        every { mealV2Repository.deleteAllByRestaurantAndDateAndType(restaurant, date, type) } just runs

        // when
        val request =
            CrawlerMealRequestDto(
                restaurant = "자하연식당 3층",
                date = date,
                type = type,
                meals = emptyList(),
            )
        useCase(request)

        // then
        verify { mealV2Repository.deleteAllByRestaurantAndDateAndType(restaurant, date, type) }
        verify(exactly = 0) { mealV2Repository.save(any()) }
        verify(exactly = 0) { mealMenuV2Repository.save(any()) }
        verify(exactly = 0) { normalizeMenuUseCase.invoke(any(), any()) }
    }
}
