package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingV2

interface BuildingV2Repository : JpaRepository<BuildingV2, Int> {
    fun findByNumber(number: String): BuildingV2?

    fun findAllByNumberIn(numbers: Collection<String>): List<BuildingV2>

    @Query(
        """
        select b
        from building_v2 b
        order by b.defaultOrder asc, b.id asc
        """,
    )
    fun findAllForList(): List<BuildingV2>
}
