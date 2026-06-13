package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantCustomV2Pk

interface RestaurantCustomV2Repository : JpaRepository<RestaurantCustomV2, RestaurantCustomV2Pk> {
    @Query(
        """
        select c
        from restaurant_custom_v2 c
        join fetch c.building b
        join fetch c.restaurant r
        where c.user.id = :userId
        """,
    )
    fun findAllByUserId(
        @Param("userId") userId: Int,
    ): List<RestaurantCustomV2>

    @Query(
        """
        select c
        from restaurant_custom_v2 c
        join fetch c.building b
        join fetch c.restaurant r
        where c.user.id = :userId
          and r.id = :restaurantId
        """,
    )
    fun findRestaurantCustomV2ByUserIdAndRestaurantId(
        @Param("userId") userId: Int,
        @Param("restaurantId") restaurantId: Int,
    ): RestaurantCustomV2?
}
