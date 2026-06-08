package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2

interface RestaurantV2Repository : JpaRepository<RestaurantV2, Int> {
    fun findByName(name: String): RestaurantV2?

    @Query(
        """
        select r
        from restaurant_v2 r
        join fetch r.building b
        order by b.sortOrder asc, r.displayOrder asc, r.id asc
        """,
    )
    fun findAllForList(): List<RestaurantV2>

    @Query(
        """
        select r
        from restaurant_v2 r
        join fetch r.building b
        where b.number = :buildingNumber
          and r.name = :name
        """,
    )
    fun findByBuildingNumberAndName(
        @Param("buildingNumber") buildingNumber: String,
        @Param("name") name: String,
    ): RestaurantV2?

    @Query(
        """
        select r
        from restaurant_v2 r
        join fetch r.building b
        where b.number = :buildingNumber
        order by r.displayOrder asc, r.id asc
        """,
    )
    fun findAllByBuildingNumber(
        @Param("buildingNumber") buildingNumber: String,
    ): List<RestaurantV2>
}
