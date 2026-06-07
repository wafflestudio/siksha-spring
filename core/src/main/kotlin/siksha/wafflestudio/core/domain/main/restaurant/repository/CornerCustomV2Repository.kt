package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.restaurant.data.CornerCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.CornerCustomV2Pk

interface CornerCustomV2Repository : JpaRepository<CornerCustomV2, CornerCustomV2Pk> {
    fun findCornerCustomV2ByUserIdAndCornerId(
        userId: Int,
        cornerId: Int,
    ): CornerCustomV2?

    fun findAllByUserId(userId: Int): List<CornerCustomV2>
}
