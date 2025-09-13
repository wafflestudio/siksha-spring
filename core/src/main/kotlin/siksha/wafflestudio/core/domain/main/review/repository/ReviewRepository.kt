package siksha.wafflestudio.core.domain.main.review.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import siksha.wafflestudio.core.domain.main.review.data.Review

@Repository
interface ReviewRepository : JpaRepository<Review, Int> {
    @Query(
        """
        SELECT r FROM review r
        WHERE r.menu.id = :menuId
        ORDER BY r.createdAt DESC
    """,
    )
    fun findByMenuIdOrderByCreatedAtDesc(
        menuId: Int,
        pageable: Pageable,
    ): List<Review>

    @Query(
        """
        SELECT COUNT(r) FROM review r
        WHERE r.menu.id = :menuId
    """,
    )
    fun countByMenuId(menuId: Int): Long

    @Query(
        value = """
    SELECT * FROM review r
    WHERE r.menu_id = :menuId
    AND (:comment IS NULL OR (:comment = true AND r.comment IS NOT NULL))
    AND (:etc IS NULL OR (:etc = true AND JSON_EXTRACT(r.etc, '$.images') IS NOT NULL))
    """,
        nativeQuery = true,
    )
    fun findFilteredReviews(
        @Param("menuId") menuId: Int,
        @Param("comment") comment: Boolean?,
        @Param("etc") etc: Boolean?,
        pageable: Pageable,
    ): List<Review>

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
    SELECT r FROM review r
    WHERE r.menu.id = :menuId AND r.user.id = :userId
""",
    )
    fun findByMenuIdAndUserId(
        @Param("menuId") menuId: Int,
        @Param("userId") userId: Int,
    ): Review?

    @Query(
        value = """
    SELECT * FROM review
    WHERE user_id = :userId
    """,
        nativeQuery = true,
    )
    fun findByUserId(
        @Param("userId") userId: Int,
        pageable: Pageable,
    ): List<Review>

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

    // 유저가 리뷰한 서로 다른 레스토랑의 총 개수
    @Query("""
        SELECT count(DISTINCT m.restaurant.id)
        FROM review r
        JOIN r.menu m
        WHERE r.user.id = :userId
    """)
    fun countDistinctRestaurantsByUserId(@Param("userId") userId: Int): Long

    // 유저가 리뷰한 레스토랑 ID들을, 해당 레스토랑에서의 최신 리뷰시각 내림차순으로 정렬하여 페이징
    @Query("""
        SELECT m.restaurant.id
        FROM review r
        JOIN r.menu m
        WHERE r.user.id = :userId
        GROUP BY m.restaurant.id
        ORDER BY max(r.createdAt) DESC, m.restaurant.id ASC
    """)
    fun findRestaurantIdsByUserIdPaged(
        @Param("userId") userId: Int,
        pageable: Pageable
    ): List<Int>

    // 선택된 레스토랑들에 대해, 해당 유저의 리뷰를 모두 조회
    @Query("""
        SELECT r
        FROM review r
        JOIN FETCH r.menu m
        JOIN FETCH m.restaurant rest
        WHERE r.user.id = :userId
          AND rest.id IN :restaurantIds
        ORDER BY rest.id ASC, r.createdAt DESC
    """)
    fun findAllByUserIdAndRestaurantIds(
        @Param("userId") userId: Int,
        @Param("restaurantIds") restaurantIds: List<Int>,
    ): List<Review>
}
