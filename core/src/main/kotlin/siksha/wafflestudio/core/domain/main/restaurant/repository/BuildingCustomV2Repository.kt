package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.BuildingCustomV2Pk

interface BuildingCustomV2Repository : JpaRepository<BuildingCustomV2, BuildingCustomV2Pk> {
    @Query(
        """
        select c
        from building_custom_v2 c
        join fetch c.building b
        where c.user.id = :userId
        """,
    )
    fun findAllByUserId(
        @Param("userId") userId: Int,
    ): List<BuildingCustomV2>

    @Query(
        """
        select c
        from building_custom_v2 c
        join fetch c.building b
        where c.user.id = :userId
          and b.number = :buildingNumber
        """,
    )
    fun findByUserIdAndBuildingNumber(
        @Param("userId") userId: Int,
        @Param("buildingNumber") buildingNumber: String,
    ): BuildingCustomV2?
}
