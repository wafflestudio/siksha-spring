package siksha.wafflestudio.core.domain.main.menu.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.menu.data.MenuLike
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface MenuLikeRepository : JpaRepository<MenuLike, Int> {
    @Modifying
    @Query(
        value = """
            INSERT INTO menu_like (user_id, menu_id, is_liked)
            VALUES (:userId, :menuId, 1)
            ON DUPLICATE KEY UPDATE is_liked = 1
        """,
        nativeQuery = true
    )
    fun postMenuLike(
        @Param("userId") userId: Int,
        @Param("menuId") menuId: Int
    )

    @Modifying
    @Query(
        value = """
            DELETE ml
            FROM menu_like ml
            JOIN menu m ON ml.menu_id = m.id
            WHERE ml.user_id      = :userId
              AND ml.is_liked     = 1
              AND m.restaurant_id = :restaurantId
              AND m.code          = :code
          """,
        nativeQuery = true
    )
    fun deleteMenuLike(
        @Param("userId")        userId: Int,
        @Param("restaurantId")  restaurantId: Int,
        @Param("code")          code: String
    )

}
