package siksha.wafflestudio.core.domain.main.menu.repository

import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import siksha.wafflestudio.core.domain.main.menu.data.Menu
import siksha.wafflestudio.core.domain.main.menu.dto.MenuLikeCount
import siksha.wafflestudio.core.domain.main.menu.dto.MenuLikeSummary
import siksha.wafflestudio.core.domain.main.menu.dto.MenuSummary

interface MenuRepository : JpaRepository<Menu, Int> {
    @Query("""
        SELECT m.id, m.restaurant_id, m.code, m.date, m.type, m.name_kr, m.name_en, m.price, m.etc,
            CONVERT_TZ(m.created_at, '+00:00', '+09:00') AS created_at,
            CONVERT_TZ(m.updated_at, '+00:00', '+09:00') AS updated_at,
            agg.score AS score,
            IFNULL(agg.review_cnt, 0) AS review_cnt
        FROM menu m
            LEFT JOIN (
                SELECT me.restaurant_id, me.code, AVG(r.score) AS score, COUNT(r.id) AS review_cnt
                FROM menu me LEFT JOIN review r ON me.id = r.menu_id
                GROUP BY me.restaurant_id, me.code
            ) agg ON m.restaurant_id = agg.restaurant_id AND m.code = agg.code
        WHERE m.date BETWEEN :start_date AND :end_date;
        """, nativeQuery = true)
    fun findMenusByDate(@Param("start_date") startDate: String, @Param("end_date") endDate: String): List<MenuSummary>;

    @Query(
        """
        SELECT m.id, COUNT(ml.id) AS like_cnt, MAX(IF(ml.user_id = :user_id, 1, 0)) AS is_liked
        FROM menu m
        JOIN menu me ON m.restaurant_id = me.restaurant_id AND m.code = me.code
        LEFT OUTER JOIN siksha.menu_like ml ON me.id = ml.menu_id AND ml.is_liked = 1
        WHERE m.date >= :start_date AND m.date <= :end_date
        GROUP BY m.id
    """, nativeQuery = true)
    fun findMenuLikesByDateAndUserId(@Param("user_id") userId: String, @Param("start_date") startDate: String, @Param("end_date") endDate: String): List<MenuLikeSummary>

    @Query(
        """
        SELECT m.id, m.restaurant_id, m.code, m.date, m.type, m.name_kr, m.name_en, m.price, m.etc,
            CONVERT_TZ(m.created_at, '+00:00', '+09:00') AS created_at,
            CONVERT_TZ(m.updated_at, '+00:00', '+09:00') AS updated_at,
            agg.score AS score,
            IFNULL(agg.review_cnt, 0) AS review_cnt
        FROM menu m
            LEFT JOIN (
                SELECT me.restaurant_id, me.code, AVG(r.score) AS score, COUNT(r.id) AS review_cnt
                FROM menu me LEFT JOIN review r ON me.id = r.menu_id
                GROUP BY me.restaurant_id, me.code
            ) agg ON m.restaurant_id = agg.restaurant_id AND m.code = agg.code
        WHERE m.id = :menu_id;
        """, nativeQuery = true
    )
    fun findMenuById(@Param("menu_id") menuId: String): MenuSummary

    @Query("""
        SELECT m.id, agg.like_cnt AS like_cnt
        FROM menu m
        LEFT JOIN (
            SELECT m.restaurant_id, m.code, COUNT(ml.id) as like_cnt
            FROM menu m
                     LEFT JOIN menu_like ml ON m.id = ml.menu_id AND ml.is_liked = 1
            GROUP BY m.restaurant_id, m.code
        ) agg ON m.restaurant_id = agg.restaurant_id AND m.code = agg.code
        WHERE m.id = :menu_id;
    """, nativeQuery = true)
    fun findMenuLikeByMenuId(@Param("menu_id") menuId: String): MenuLikeCount

    @Query("""
        SELECT m.id, COUNT(ml.id) AS like_cnt, MAX(IF(ml.user_id = :user_id, 1, 0)) AS is_liked
        FROM menu m
        JOIN menu me ON m.restaurant_id = me.restaurant_id AND m.code = me.code
        LEFT OUTER JOIN menu_like  ml ON me.id = ml.menu_id AND ml.is_liked = 1
        WHERE m.id = :menu_id
        GROUP BY m.id
    """, nativeQuery = true)
    fun findMenuLikeByMenuIdAndUserId(@Param("menu_id") menuId: String, userId: String): MenuLikeSummary
}
