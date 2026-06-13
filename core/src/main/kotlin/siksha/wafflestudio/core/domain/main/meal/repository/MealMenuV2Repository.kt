package siksha.wafflestudio.core.domain.main.meal.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.main.meal.data.MealMenuV2
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2DetailRow
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2MealContextRow
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2MealRow
import java.time.LocalDate

interface MealMenuV2Repository : JpaRepository<MealMenuV2, Long> {
    @Query(
        """
        select
            mm.id as mealMenuId,
            meal.id as mealId,
            menu.id as menuId,
            menu.name as menuName,
            mm.original_name as originalName,
            restaurant.id as restaurantId,
            meal.date as date,
            meal.type as type,
            meal.price as price,
            meal.no_meat as noMeat,
            menu.created_at as menuCreatedAt,
            review_stats.score as score,
            ifnull(review_stats.reviewCnt, 0) as reviewCnt,
            ifnull(like_stats.likeCnt, 0) as likeCnt,
            case when user_like.id is null then 0 else 1 end as isLiked
        from meal_menu_v2 mm
        join meal_v2 meal on meal.id = mm.meal_id
        join menu_v2 menu on menu.id = mm.menu_id
        join restaurant_v2 restaurant on restaurant.id = meal.restaurant_id
        left join (
            select review.menu_id as menuId, avg(review.score) as score, count(review.id) as reviewCnt
            from review_v2 review
            group by review.menu_id
        ) review_stats on review_stats.menuId = menu.id
        left join (
            select menu_like.menu_id as menuId, count(menu_like.id) as likeCnt
            from menu_like_v2 menu_like
            where coalesce(menu_like.is_liked, 1) = 1
            group by menu_like.menu_id
        ) like_stats on like_stats.menuId = menu.id
        left join menu_like_v2 user_like
            on user_like.menu_id = menu.id
            and user_like.user_id = :userId
            and coalesce(user_like.is_liked, 1) = 1
        where meal.date between :startDate and :endDate
        order by meal.date asc, meal.type asc, meal.id asc, mm.id asc
        """,
        nativeQuery = true,
    )
    fun findMenuRowsByDate(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        @Param("userId") userId: Int,
    ): List<MenuV2MealRow>

    @Query(
        """
        select
            menu.id as menuId,
            menu.name as menuName,
            restaurant.id as restaurantId,
            restaurant.name as restaurantName,
            building.id as buildingId,
            building.number as buildingNumber,
            building.name as buildingName,
            menu.created_at as menuCreatedAt,
            review_stats.score as score,
            ifnull(review_stats.reviewCnt, 0) as reviewCnt,
            ifnull(like_stats.likeCnt, 0) as likeCnt,
            case when user_like.id is null then 0 else 1 end as isLiked
        from menu_v2 menu
        join restaurant_v2 restaurant on restaurant.id = menu.restaurant_id
        join building_v2 building on building.id = restaurant.building_id
        left join (
            select review.menu_id as menuId, avg(review.score) as score, count(review.id) as reviewCnt
            from review_v2 review
            group by review.menu_id
        ) review_stats on review_stats.menuId = menu.id
        left join (
            select menu_like.menu_id as menuId, count(menu_like.id) as likeCnt
            from menu_like_v2 menu_like
            where coalesce(menu_like.is_liked, 1) = 1
            group by menu_like.menu_id
        ) like_stats on like_stats.menuId = menu.id
        left join menu_like_v2 user_like
            on user_like.menu_id = menu.id
            and user_like.user_id = :userId
            and coalesce(user_like.is_liked, 1) = 1
        where menu.id = :menuId
        """,
        nativeQuery = true,
    )
    fun findMenuDetailById(
        @Param("menuId") menuId: Long,
        @Param("userId") userId: Int,
    ): MenuV2DetailRow?

    @Query(
        """
        select
            mm.id as mealMenuId,
            meal.id as mealId,
            meal.date as date,
            meal.type as type,
            meal.price as price,
            meal.no_meat as noMeat,
            mm.original_name as originalName,
            meal.created_at as mealCreatedAt
        from meal_menu_v2 mm
        join meal_v2 meal on meal.id = mm.meal_id
        where mm.menu_id = :menuId
        order by meal.date desc, meal.type asc, meal.id desc, mm.id desc
        """,
        nativeQuery = true,
    )
    fun findMealContextsByMenuId(
        @Param("menuId") menuId: Long,
    ): List<MenuV2MealContextRow>
}
