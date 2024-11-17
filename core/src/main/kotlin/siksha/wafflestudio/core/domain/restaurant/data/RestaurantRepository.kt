package siksha.wafflestudio.core.domain.restaurant.data

import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantRepository : JpaRepository<Restaurant, Long>
