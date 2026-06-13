package siksha.wafflestudio.core.domain.main.restaurant.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantLikeV2
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantLikeV2Pk

interface RestaurantLikeV2Repository : JpaRepository<RestaurantLikeV2, RestaurantLikeV2Pk> {
    fun findRestaurantLikeV2ByUserIdAndRestaurantId(
        userId: Int,
        restaurantId: Int,
    ): RestaurantLikeV2?

    @Query(
        """
        select l
        from restaurant_like_v2 l
        join fetch l.restaurant r
        where l.user.id = :userId
        """,
    )
    fun findAllByUserId(
        @Param("userId") userId: Int,
    ): List<RestaurantLikeV2>
}
