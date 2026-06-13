package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingCustomV2

interface BuildingCustomV2Repository : JpaRepository<BuildingCustomV2, Int> {
    fun findByUserId(userId: Int): BuildingCustomV2?
}
