package siksha.wafflestudio.core.domain.main.review.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.main.review.data.KeywordReview
import siksha.wafflestudio.core.domain.main.review.dto.KeywordReviewSummary

@Repository
interface KeywordReviewRepository : JpaRepository<KeywordReview, Int> {
    @Query(
        """
            SELECT
                taste_stats.taste_keyword,
                taste_stats.taste_cnt,
                totals.taste_total,
                price_stats.price_keyword,
                price_stats.price_cnt,
                totals.price_total,
                food_stats.food_composition_keyword,
                food_stats.food_composition_cnt,
                totals.food_composition_total
            FROM
                (SELECT 1) AS dummy
            LEFT JOIN
                (
                    SELECT taste AS taste_keyword, COUNT(*) AS taste_cnt
                    FROM keyword_review
                    WHERE restaurant_id = :restaurant_id AND menu_code = :code AND taste != -1
                    GROUP BY taste
                    ORDER BY taste_cnt DESC
                    LIMIT 1
                ) AS taste_stats ON 1=1
            LEFT JOIN
                (
                    SELECT price AS price_keyword, COUNT(*) AS price_cnt
                    FROM keyword_review
                    WHERE restaurant_id = :restaurant_id AND menu_code = :code AND price != -1
                    GROUP BY price
                    ORDER BY price_cnt DESC
                    LIMIT 1
                ) AS price_stats ON 1=1
            LEFT JOIN
                (
                    SELECT food_composition AS food_composition_keyword, COUNT(*) AS food_composition_cnt
                    FROM keyword_review
                    WHERE restaurant_id = :restaurant_id AND menu_code = :code AND food_composition != -1
                    GROUP BY food_composition
                    ORDER BY food_composition_cnt DESC
                    LIMIT 1
                ) AS food_stats ON 1=1
            LEFT JOIN
                (
                    SELECT
                        COUNT(CASE WHEN taste != -1 THEN 1 END) AS taste_total,
                        COUNT(CASE WHEN price != -1 THEN 1 END) AS price_total,
                        COUNT(CASE WHEN food_composition != -1 THEN 1 END) AS food_composition_total
                    FROM keyword_review
                    WHERE restaurant_id = :restaurant_id AND menu_code = :code
                ) AS totals ON 1=1
        """,
        nativeQuery = true,
    )
    fun findScoreCountsByRestaurantIdAndCode(
        @Param("restaurant_id") restaurantId: Int,
        @Param("code") code: String,
    ): KeywordReviewSummary
}
