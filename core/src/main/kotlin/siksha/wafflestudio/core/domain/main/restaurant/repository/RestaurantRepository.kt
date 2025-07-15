package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.restaurant.data.Restaurant

interface RestaurantRepository : JpaRepository<Restaurant, Int>
