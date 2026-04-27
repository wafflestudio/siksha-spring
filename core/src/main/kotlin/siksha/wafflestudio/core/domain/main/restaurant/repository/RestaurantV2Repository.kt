package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2

interface RestaurantV2Repository : JpaRepository<RestaurantV2, Long> {
    fun findByName(name: String): RestaurantV2?
}
