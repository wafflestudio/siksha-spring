package siksha.wafflestudio.core.domain.main.menu.repository

import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import siksha.wafflestudio.core.domain.main.menu.data.Menu
import siksha.wafflestudio.core.domain.main.menu.dto.MenuLikeCount
import siksha.wafflestudio.core.domain.main.menu.dto.MenuLikeSummary
import siksha.wafflestudio.core.domain.main.menu.dto.MenuPlainSummary
import siksha.wafflestudio.core.domain.main.menu.dto.MenuSummary

interface MenuRepository : JpaRepository<Menu, Int> {
    @Query("""
        SELECT m.id, m.restaurant_id AS restaurantId, m.code, m.date, m.type, m.name_kr AS nameKr, m.name_en AS nameEn, m.price, m.etc,
            CONVERT_TZ(m.created_at, '+00:00', '+09:00') AS createdAt,
            CONVERT_TZ(m.updated_at, '+00:00', '+09:00') AS updatedAt,
            agg.score AS score,
            IFNULL(agg.review_cnt, 0) AS reviewCnt
        FROM menu m
            LEFT JOIN (
                SELECT me.restaurant_id, me.code, AVG(r.score) AS score, COUNT(r.id) AS review_cnt
                FROM menu me LEFT JOIN review r ON me.id = r.menu_id
                GROUP BY me.restaurant_id, me.code
            ) agg ON m.restaurant_id = agg.restaurant_id AND m.code = agg.code
        WHERE m.date BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    fun findMenusByDate(@Param("startDate") startDate: String, @Param("endDate") endDate: String): List<MenuSummary>;

    @Query(
        """
        SELECT m.id, COUNT(ml.id) AS likeCnt, MAX(IF(ml.user_id = :userId, 1, 0)) AS isLiked
        FROM menu m
        JOIN menu me ON m.restaurant_id = me.restaurant_id AND m.code = me.code
        LEFT OUTER JOIN siksha.menu_like ml ON me.id = ml.menu_id AND ml.is_liked = 1
        WHERE m.date >= :startDate AND m.date <= :endDate
        GROUP BY m.id
    """, nativeQuery = true)
    fun findMenuLikesByDateAndUserId(@Param("userId") userId: String, @Param("startDate") startDate: String, @Param("endDate") endDate: String): List<MenuLikeSummary>

    @Query(
        """
        SELECT m.id, m.restaurant_id AS restaurantId, m.code, m.date, m.type, m.name_kr AS nameKr, m.name_en AS nameEn, m.price, m.etc,
            CONVERT_TZ(m.created_at, '+00:00', '+09:00') AS createdAt,
            CONVERT_TZ(m.updated_at, '+00:00', '+09:00') AS updatedAt,
            agg.score AS score,
            IFNULL(agg.review_cnt, 0) AS reviewCnt
        FROM menu m
            LEFT JOIN (
                SELECT me.restaurant_id, me.code, AVG(r.score) AS score, COUNT(r.id) AS review_cnt
                FROM menu me LEFT JOIN review r ON me.id = r.menu_id
                GROUP BY me.restaurant_id, me.code
            ) agg ON m.restaurant_id = agg.restaurant_id AND m.code = agg.code
        WHERE m.id = :menuId;
        """, nativeQuery = true
    )
    fun findMenuById(@Param("menuId") menuId: String): MenuSummary

    @Query("""
        SELECT m.id, m.restaurant_id AS restaurantId, m.code
        FROM menu m
        WHERE m.id = :menuId
    """, nativeQuery = true)
    fun findPlainMenuById(menuId: String): MenuPlainSummary

    @Query("""
        SELECT m.id, agg.like_cnt AS likeCnt
        FROM menu m
        LEFT JOIN (
            SELECT m.restaurant_id, m.code, COUNT(ml.id) as like_cnt
            FROM menu m
                     LEFT JOIN menu_like ml ON m.id = ml.menu_id AND ml.is_liked = 1
            GROUP BY m.restaurant_id, m.code
        ) agg ON m.restaurant_id = agg.restaurant_id AND m.code = agg.code
        WHERE m.id = :menuId
    """, nativeQuery = true)
    fun findMenuLikeByMenuId(@Param("menuId") menuId: String): MenuLikeCount

    @Query("""
        SELECT m.id, COUNT(ml.id) AS likeCnt, MAX(IF(ml.user_id = :userId, 1, 0)) AS isLiked
        FROM menu m
        JOIN menu me ON m.restaurant_id = me.restaurant_id AND m.code = me.code
        LEFT OUTER JOIN menu_like  ml ON me.id = ml.menu_id AND ml.is_liked = 1
        WHERE m.id = :menuId
        GROUP BY m.id
    """, nativeQuery = true)
    fun findMenuLikeByMenuIdAndUserId(@Param("menuId") menuId: String, @Param("userId") userId: String): MenuLikeSummary
}
