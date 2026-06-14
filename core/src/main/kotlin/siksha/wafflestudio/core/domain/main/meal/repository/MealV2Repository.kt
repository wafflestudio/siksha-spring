package siksha.wafflestudio.core.domain.main.meal.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.main.meal.data.MealType
import siksha.wafflestudio.core.domain.main.meal.data.MealV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2
import java.time.LocalDate

interface MealV2Repository : JpaRepository<MealV2, Long> {
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        delete from meal_v2 meal
        where meal.restaurant = :restaurant
          and meal.date = :date
          and meal.type = :type
        """,
    )
    fun deleteAllByRestaurantAndDateAndType(
        @Param("restaurant") restaurant: RestaurantV2,
        @Param("date") date: LocalDate,
        @Param("type") type: MealType,
    ): Int
}
