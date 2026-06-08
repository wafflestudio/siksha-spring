package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2Pk

interface RestaurantCustomV2Repository : JpaRepository<RestaurantCustomV2, RestaurantCustomV2Pk> {
    fun findRestaurantCustomV2ByUserIdAndRestaurantId(
        userId: Int,
        restaurantId: Int,
    ): RestaurantCustomV2?

    fun findAllByUserId(userId: Int): List<RestaurantCustomV2>

    @Query(
        """
        select c
        from restaurant_custom_v2 c
        join fetch c.restaurant r
        join fetch r.building b
        where c.user.id = :userId
          and b.number = :buildingNumber
        """,
    )
    fun findAllByUserIdAndBuildingNumber(
        @Param("userId") userId: Int,
        @Param("buildingNumber") buildingNumber: String,
    ): List<RestaurantCustomV2>
}
