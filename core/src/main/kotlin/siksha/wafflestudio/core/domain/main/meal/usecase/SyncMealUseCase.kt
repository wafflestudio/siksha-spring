package siksha.wafflestudio.core.domain.main.meal.usecase

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import siksha.wafflestudio.core.domain.common.exception.RestaurantNotFound
import siksha.wafflestudio.core.domain.main.meal.data.MealMenuV2
import siksha.wafflestudio.core.domain.main.meal.data.MealV2
import siksha.wafflestudio.core.domain.main.meal.dto.CrawlerMealRequestDto
import siksha.wafflestudio.core.domain.main.meal.repository.MealMenuV2Repository
import siksha.wafflestudio.core.domain.main.meal.repository.MealV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.repository.RestaurantV2Repository

@Component
class SyncMealUseCase(
    private val restaurantV2Repository: RestaurantV2Repository,
    private val mealV2Repository: MealV2Repository,
    private val mealMenuV2Repository: MealMenuV2Repository,
    private val normalizeMenuUseCase: NormalizeMenuUseCase,
) {
    @Transactional
    operator fun invoke(request: CrawlerMealRequestDto) {
        val restaurant = restaurantV2Repository.findByName(request.restaurant) ?: throw RestaurantNotFound()

        mealV2Repository.deleteAllByRestaurantAndDateAndType(restaurant, request.date, request.type)

        request.meals.forEach { mealItem ->
            val meal =
                mealV2Repository.save(
                    MealV2(
                        restaurant = restaurant,
                        date = request.date,
                        type = request.type,
                        price = mealItem.price,
                        noMeat = mealItem.noMeat,
                    ),
                )

            mealItem.menus.forEach { originalName ->
                val menu = normalizeMenuUseCase(originalName, restaurant)
                mealMenuV2Repository.save(
                    MealMenuV2(
                        meal = meal,
                        menu = menu,
                        originalName = originalName,
                    ),
                )
            }
        }
    }
}
