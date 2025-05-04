package siksha.wafflestudio.core.domain.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.restaurant.data.Restaurant

interface RestaurantRepository : JpaRepository<Restaurant, Int>
