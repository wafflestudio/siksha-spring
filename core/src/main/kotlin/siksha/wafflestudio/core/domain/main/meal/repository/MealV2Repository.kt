package siksha.wafflestudio.core.domain.main.meal.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.meal.data.MealType
import siksha.wafflestudio.core.domain.main.meal.data.MealV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import java.time.LocalDate

interface MealV2Repository : JpaRepository<MealV2, Long> {
    fun deleteAllByRestaurantAndDateAndType(restaurant: RestaurantV2, date: LocalDate, type: MealType)
}
