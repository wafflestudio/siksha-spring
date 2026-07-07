package siksha.wafflestudio.core.domain.main.menu.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.main.menu.data.MenuAlarmV2
import siksha.wafflestudio.core.domain.main.menu.data.MenuAlarmV2Id
import siksha.wafflestudio.core.domain.v1.main.menu.dto.AlarmMenuSummary
import siksha.wafflestudio.core.domain.v1.main.menu.dto.MenuPlainSummary
import java.time.LocalDate

interface MenuAlarmV2Repository : JpaRepository<MenuAlarmV2, MenuAlarmV2Id> {
    @Query(
        value = """
            select count(*) > 0
            from menu_alarm_v2
            where user_id = :userId
              and menu_id = :menuId
        """,
        nativeQuery = true,
    )
    fun existsByUserIdAndMenuId(
        @Param("userId") userId: Int,
        @Param("menuId") menuId: Long,
    ): Boolean

    @Modifying
    @Query(
        value = """
            insert into menu_alarm_v2 (user_id, menu_id)
            values (:userId, :menuId)
        """,
        nativeQuery = true,
    )
    fun postMenuAlarm(
        @Param("userId") userId: Int,
        @Param("menuId") menuId: Long,
    )

    @Modifying
    @Query(
        value = """
            delete from menu_alarm_v2
            where user_id = :userId
              and menu_id = :menuId
        """,
        nativeQuery = true,
    )
    fun deleteMenuAlarm(
        @Param("userId") userId: Int,
        @Param("menuId") menuId: Long,
    )

    @Modifying
    @Query(
        value = """
            delete from menu_alarm_v2
            where user_id = :userId
        """,
        nativeQuery = true,
    )
    fun deleteMenuAlarmByUserId(
        @Param("userId") userId: Int,
    )

    @Modifying
    @Query(
        value = """
            insert into menu_alarm_v2 (user_id, menu_id)
            select ml.user_id, ml.menu_id
            from menu_like_v2 ml
            left join menu_alarm_v2 ma
                on ma.user_id = ml.user_id
                and ma.menu_id = ml.menu_id
            where ml.user_id = :userId
              and coalesce(ml.is_liked, 1) = 1
              and ma.menu_id is null
        """,
        nativeQuery = true,
    )
    fun postAllLikedMenuAlarms(
        @Param("userId") userId: Int,
    )

    @Query(
        value = """
            select
                cast(menu.id as signed) as id,
                ma.user_id as userId,
                menu.name as nameKr,
                restaurant.id as restaurantId,
                restaurant.name as restaurantName,
                concat('v2:', menu.id) as code
            from menu_alarm_v2 ma
            join menu_v2 menu on menu.id = ma.menu_id
            join restaurant_v2 restaurant on restaurant.id = menu.restaurant_id
            where ma.user_id in :userIds
        """,
        nativeQuery = true,
    )
    fun findMenuAlarmByUserIds(
        @Param("userIds") userIds: List<Int>,
    ): List<AlarmMenuSummary>

    @Query(
        value = """
            select
                cast(menu.id as signed) as id,
                restaurant.id as restaurantId,
                concat('v2:', menu.id) as code
            from meal_menu_v2 mm
            join meal_v2 meal on meal.id = mm.meal_id
            join menu_v2 menu on menu.id = mm.menu_id
            join restaurant_v2 restaurant on restaurant.id = meal.restaurant_id
            where meal.date = :date
            group by menu.id, restaurant.id
        """,
        nativeQuery = true,
    )
    fun findAllAlarmMenusByDate(
        @Param("date") date: LocalDate,
    ): List<MenuPlainSummary>

    @Query(
        value = """
            select
                cast(menu.id as signed) as id,
                restaurant.id as restaurantId,
                concat('v2:', menu.id) as code
            from meal_menu_v2 mm
            join meal_v2 meal on meal.id = mm.meal_id
            join menu_v2 menu on menu.id = mm.menu_id
            join restaurant_v2 restaurant on restaurant.id = meal.restaurant_id
            where meal.date = :date
              and meal.type = :type
            group by menu.id, restaurant.id
        """,
        nativeQuery = true,
    )
    fun findAllAlarmMenusByDateAndType(
        @Param("date") date: LocalDate,
        @Param("type") type: String,
    ): List<MenuPlainSummary>
}
