package siksha.wafflestudio.core.domain.main.review.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.main.review.data.Review
import siksha.wafflestudio.core.domain.main.review.dto.ReviewSummary

@Repository
interface ReviewRepository : JpaRepository<Review, Int> {
    @Query(
        value = """
        SELECT 
            r.id, r.menu_id AS menuId, r.user_id AS userId, r.score, r.comment, r.etc, 
            kr.flavor, kr.price, kr.food_composition, 
            IFNULL(rl.like_count, 0) AS like_count,
            CASE 
                WHEN EXISTS (
                    SELECT 1 
                    FROM review_like rl2 
                    WHERE rl2.review_id = r.id AND rl2.user_id = :userId
                ) 
                THEN 1 
                ELSE 0 
            END AS is_liked,
            r.created_at, r.updated_at 
        FROM review r
        LEFT JOIN keyword_review kr ON r.id = kr.review_id
        LEFT JOIN (
            SELECT review_id, COUNT(*) AS like_count
            FROM review_like
            GROUP BY review_id
        ) rl ON r.id = rl.review_id
        WHERE r.menu_id = :menuId
        ORDER BY r.created_at DESC
    """,
        nativeQuery = true
    )
    fun findByMenuIdOrderByCreatedAtDesc(
        @Param("userId") userId: Int,
        @Param("menuId") menuId: Int,
        pageable: Pageable,
    ): List<ReviewSummary>

    @Query(
        """
        SELECT COUNT(r) FROM review r
        WHERE r.menu.id = :menuId
    """,
    )
    fun countByMenuId(menuId: Int): Long

    @Query(
        value = """
        SELECT 
            r.id, r.menu_id AS menuId, r.user_id AS userId, r.score, r.comment, r.etc, 
            kr.flavor, kr.price, kr.food_composition, 
            IFNULL(rl.like_count, 0) AS like_count,
            CASE 
                WHEN EXISTS (
                    SELECT 1 
                    FROM review_like rl2 
                    WHERE rl2.review_id = r.id AND rl2.user_id = :userId
                ) 
                THEN 1 
                ELSE 0 
            END AS is_liked,
            r.created_at, r.updated_at 
        FROM review r
        LEFT JOIN keyword_review kr ON r.id = kr.review_id
        LEFT JOIN (
            SELECT review_id, COUNT(*) AS like_count
            FROM review_like
            GROUP BY review_id
        ) rl ON r.id = rl.review_id
        WHERE r.menu_id = :menuId
        AND (:comment IS NULL OR (:comment = true AND r.comment IS NOT NULL))
        AND (:etc IS NULL OR (:etc = true AND JSON_EXTRACT(r.etc, '$.images') IS NOT NULL))
    """,
        nativeQuery = true,
    )
    fun findFilteredReviews(
        @Param("userId") userId: Int,
        @Param("menuId") menuId: Int,
        @Param("comment") comment: Boolean?,
        @Param("etc") etc: Boolean?,
        pageable: Pageable,
    ): List<ReviewSummary>

    @Query(
        value = """
        SELECT COUNT(*) FROM review r
        WHERE r.menu_id = :menuId
        AND (:comment IS NULL OR (:comment = true AND r.comment IS NOT NULL))
        AND (:imageExist IS NULL OR (:imageExist = true AND JSON_EXTRACT(r.etc, '$.images') IS NOT NULL))
    """,
        nativeQuery = true,
    )
    fun countFilteredReviews(
        @Param("menuId") menuId: Int,
        @Param("comment") comment: Boolean?,
        @Param("imageExist") imageExist: Boolean?,
    ): Long

    @Query(
        """
    SELECT r.id, r.menu_id AS menuId, r.user_id AS userId, r.score, r.comment, r.etc, kr.flavor, kr.price, kr.food_composition, r.created_at, r.updated_at 
    FROM review r
    Left JOIN keyword_review kr ON r.id = kr.review_id
    WHERE r.menu.id = :menuId AND r.user.id = :userId
""",
        nativeQuery = true,
    )
    fun findByMenuIdAndUserId(
        @Param("menuId") menuId: Int,
        @Param("userId") userId: Int,
    ): ReviewSummary?

    @Query(
        value = """
        SELECT 
            r.id, r.menu_id AS menuId, r.user_id AS userId, r.score, r.comment, r.etc, 
            kr.flavor, kr.price, kr.food_composition, 
            IFNULL(rl.like_count, 0) AS like_count,
            CASE 
                WHEN EXISTS (
                    SELECT 1 
                    FROM review_like rl2 
                    WHERE rl2.review_id = r.id AND rl2.user_id = :userId
                ) 
                THEN 1 
                ELSE 0 
            END AS is_liked,
            r.created_at, r.updated_at 
        FROM review r
        LEFT JOIN keyword_review kr ON r.id = kr.review_id
        LEFT JOIN (
            SELECT review_id, COUNT(*) AS like_count
            FROM review_like
            GROUP BY review_id
        ) rl ON r.id = rl.review_id
        WHERE user_id = :userId
    """,
        nativeQuery = true,
    )
    fun findByUserId(
        @Param("userId") userId: Int,
        pageable: Pageable,
    ): List<ReviewSummary>

    @Query(
        value = """
        SELECT COUNT(*) FROM review
        WHERE user_id = :userId
    """,
        nativeQuery = true,
    )
    fun countByUserId(
        @Param("userId") userId: Int,
    ): Long

    @Query(
        value = """
        SELECT comment FROM review
        WHERE score = :score AND comment IS NOT NULL
        ORDER BY RAND()
        LIMIT 1
    """,
        nativeQuery = true,
    )
    fun findRandomCommentByScore(
        @Param("score") score: Int,
    ): String?

    @Query(
        value = """
        SELECT score, COUNT(*) as cnt 
        FROM review 
        WHERE menu_id = :menuId 
        GROUP BY score
    """,
        nativeQuery = true,
    )
    fun findScoreCountsByMenuId(
        @Param("menuId") menuId: Int,
    ): List<Array<Any>>
}
