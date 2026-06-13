package siksha.wafflestudio.core.domain.main.menu.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.main.menu.data.MenuLikeV2
import siksha.wafflestudio.core.domain.main.menu.dto.MenuV2LikedMenuRow

interface MenuLikeV2Repository : JpaRepository<MenuLikeV2, Long> {
    @Query(
        value = """
            select count(*) > 0
            from menu_like_v2
            where user_id = :userId
              and menu_id = :menuId
              and coalesce(is_liked, 1) = 1
        """,
        nativeQuery = true,
    )
    fun existsLikedMenu(
        @Param("userId") userId: Int,
        @Param("menuId") menuId: Long,
    ): Boolean

    @Modifying
    @Query(
        value = """
            insert into menu_like_v2 (user_id, menu_id, is_liked)
            values (:userId, :menuId, 1)
            on duplicate key update is_liked = 1
        """,
        nativeQuery = true,
    )
    fun likeMenu(
        @Param("userId") userId: Int,
        @Param("menuId") menuId: Long,
    )

    @Modifying
    @Query(
        value = """
            update menu_like_v2
            set is_liked = 0
            where user_id = :userId
              and menu_id = :menuId
        """,
        nativeQuery = true,
    )
    fun unlikeMenu(
        @Param("userId") userId: Int,
        @Param("menuId") menuId: Long,
    )

    @Query(
        value = """
            select
                menu.id as menuId,
                menu.name as menuName,
                restaurant.id as restaurantId,
                menu.created_at as menuCreatedAt,
                review_stats.score as score,
                ifnull(review_stats.reviewCnt, 0) as reviewCnt,
                ifnull(like_stats.likeCnt, 0) as likeCnt,
                case when menu_alarm.menu_id is null then 0 else 1 end as alarm
            from menu_like_v2 my_like
            join menu_v2 menu on menu.id = my_like.menu_id
            join restaurant_v2 restaurant on restaurant.id = menu.restaurant_id
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
            left join menu_alarm_v2 menu_alarm
                on menu_alarm.user_id = my_like.user_id
                and menu_alarm.menu_id = menu.id
            where my_like.user_id = :userId
              and coalesce(my_like.is_liked, 1) = 1
            order by menu.name asc, menu.id asc
        """,
        nativeQuery = true,
    )
    fun findLikedMenusByUserId(
        @Param("userId") userId: Int,
    ): List<MenuV2LikedMenuRow>
}
