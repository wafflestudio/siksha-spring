package siksha.wafflestudio.core.domain.main.meal.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.meal.data.MealMenuV2

interface MealMenuV2Repository : JpaRepository<MealMenuV2, Long>
