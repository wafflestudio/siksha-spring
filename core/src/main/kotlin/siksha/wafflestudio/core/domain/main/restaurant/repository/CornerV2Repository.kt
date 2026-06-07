package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.main.restaurant.data.CornerV2

interface CornerV2Repository : JpaRepository<CornerV2, Int> {
    @Query(
        """
        select c
        from corner_v2 c
        join fetch c.restaurant r
        join fetch r.building b
        where c.active = true
        order by b.sortOrder, r.displayOrder, c.displayOrder, c.id
        """,
    )
    fun findAllActiveForList(): List<CornerV2>

    @Query(
        """
        select c
        from corner_v2 c
        join fetch c.restaurant r
        join fetch r.building b
        where b.number = :buildingNumber
          and r.name = :restaurantName
          and c.isDefault = true
          and c.active = true
        """,
    )
    fun findActiveDefaultByBuildingNumberAndRestaurantName(
        @Param("buildingNumber") buildingNumber: String,
        @Param("restaurantName") restaurantName: String,
    ): CornerV2?

    @Query(
        """
        select c
        from corner_v2 c
        join fetch c.restaurant r
        join fetch r.building b
        where b.number = :buildingNumber
          and r.name = :restaurantName
          and c.name = :cornerName
          and c.active = true
        """,
    )
    fun findActiveByBuildingNumberAndRestaurantNameAndName(
        @Param("buildingNumber") buildingNumber: String,
        @Param("restaurantName") restaurantName: String,
        @Param("cornerName") name: String,
    ): CornerV2?
}
