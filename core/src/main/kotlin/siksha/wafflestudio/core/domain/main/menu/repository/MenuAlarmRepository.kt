package siksha.wafflestudio.core.domain.main.menu.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.main.menu.data.MenuAlarm


interface MenuAlarmRepository : JpaRepository<MenuAlarm, Int> {
    @Modifying
    @Query(
        value = """
            INSERT INTO menu_alarm (user_id, menu_id)
            VALUES (:userId, :menuId)
        """,
        nativeQuery = true,
    )
    fun postMenuAlarm(
        @Param("userId") userId: Int,
        @Param("menuId") menuId: Int,
    )

    @Query(
        value = """
            SELECT ma.*
            FROM menu_alarm ma
            JOIN menu m ON ma.menu_id = m.id
            WHERE ma.user_id      = :userId
              AND m.restaurant_id = :restaurantId
              AND m.code          = :code
        """,
        nativeQuery = true,
    )
    fun findMenuAlarm(
        @Param("userId") userId: Int,
        @Param("restaurantId") restaurantId: Int,
        @Param("code") code: String,
    ): List<MenuAlarm>

    @Modifying
    @Query(
        value = """
            DELETE ma
            FROM menu_alarm ma
            JOIN menu m ON ma.menu_id = m.id
            WHERE ma.user_id      = :userId
              AND m.restaurant_id = :restaurantId
              AND m.code          = :code
          """,
        nativeQuery = true,
    )
    fun deleteMenuAlarm(
        @Param("userId") userId: Int,
        @Param("restaurantId") restaurantId: Int,
        @Param("code") code: String,
    )

    @Query(
        """
        SELECT m.id
        FROM menu m
        JOIN menu me ON m.restaurant_id = me.restaurant_id AND m.code = me.code
        INNER JOIN menu_alarm ma ON me.id = ma.menu_id
        WHERE m.id in :menuIds AND ma.user_id = :userId
        GROUP BY m.id
    """,
        nativeQuery = true,
    )
    fun findMenuLikesByMenuIds(
        @Param("userId") userId: Int,
        @Param("menuIds") menuIds: List<Int>,
    ): List<Int>
}
