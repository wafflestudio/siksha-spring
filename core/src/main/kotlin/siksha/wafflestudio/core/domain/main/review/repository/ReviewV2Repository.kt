package siksha.wafflestudio.core.domain.main.review.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.main.review.data.ReviewV2
import siksha.wafflestudio.core.domain.main.review.dto.ReviewV2Summary

interface ReviewV2Repository : JpaRepository<ReviewV2, Long> {
    fun existsByMenuIdAndUserId(
        menuId: Long,
        userId: Int,
    ): Boolean

    @Query(
        """
        select r
        from review_v2 r
        join fetch r.menu m
        join fetch m.restaurant restaurant
        join fetch restaurant.building building
        join fetch r.user u
        where r.id = :reviewId
        """,
    )
    fun findByIdForDetail(
        @Param("reviewId") reviewId: Long,
    ): ReviewV2?

    @Query(
        value = """
        select
            r.id as id,
            r.menu_id as menuId,
            menu.name as menuName,
            restaurant.id as restaurantId,
            restaurant.name as restaurantName,
            r.user_id as userId,
            r.score as score,
            r.comment as comment,
            r.etc as etc,
            kr.taste as taste,
            kr.price as price,
            kr.food_composition as foodComposition,
            ifnull(rl.like_count, 0) as likeCount,
            case when user_like.id is null then 0 else 1 end as isLiked,
            r.created_at as createdAt,
            r.updated_at as updatedAt
        from review_v2 r
        join menu_v2 menu on menu.id = r.menu_id
        join restaurant_v2 restaurant on restaurant.id = menu.restaurant_id
        left join keyword_review_v2 kr on kr.review_id = r.id
        left join (
            select review_id, count(*) as like_count
            from review_like_v2
            group by review_id
        ) rl on rl.review_id = r.id
        left join review_like_v2 user_like
            on user_like.review_id = r.id
            and user_like.user_id = :userId
        where r.menu_id = :menuId
        order by r.created_at desc
        """,
        nativeQuery = true,
    )
    fun findByMenuIdOrderByCreatedAtDesc(
        @Param("userId") userId: Int,
        @Param("menuId") menuId: Long,
        pageable: Pageable,
    ): List<ReviewV2Summary>

    @Query(
        value = """
        select count(*)
        from review_v2 r
        where r.menu_id = :menuId
        """,
        nativeQuery = true,
    )
    fun countByMenuId(
        @Param("menuId") menuId: Long,
    ): Long

    @Query(
        value = """
        select
            r.id as id,
            r.menu_id as menuId,
            menu.name as menuName,
            restaurant.id as restaurantId,
            restaurant.name as restaurantName,
            r.user_id as userId,
            r.score as score,
            r.comment as comment,
            r.etc as etc,
            kr.taste as taste,
            kr.price as price,
            kr.food_composition as foodComposition,
            ifnull(rl.like_count, 0) as likeCount,
            case when user_like.id is null then 0 else 1 end as isLiked,
            r.created_at as createdAt,
            r.updated_at as updatedAt
        from review_v2 r
        join menu_v2 menu on menu.id = r.menu_id
        join restaurant_v2 restaurant on restaurant.id = menu.restaurant_id
        left join keyword_review_v2 kr on kr.review_id = r.id
        left join (
            select review_id, count(*) as like_count
            from review_like_v2
            group by review_id
        ) rl on rl.review_id = r.id
        left join review_like_v2 user_like
            on user_like.review_id = r.id
            and user_like.user_id = :userId
        where r.menu_id = :menuId
          and (:comment is null
              or (:comment = true and r.comment is not null and r.comment != '')
              or (:comment = false and (r.comment is null or r.comment = '')))
          and (:image is null
              or (:image = true and r.etc is not null and r.etc != '[]')
              or (:image = false and (r.etc is null or r.etc = '[]')))
        order by r.created_at desc
        """,
        nativeQuery = true,
    )
    fun findFilteredReviews(
        @Param("userId") userId: Int,
        @Param("menuId") menuId: Long,
        @Param("comment") comment: Boolean?,
        @Param("image") image: Boolean?,
        pageable: Pageable,
    ): List<ReviewV2Summary>

    @Query(
        value = """
        select count(*)
        from review_v2 r
        where r.menu_id = :menuId
          and (:comment is null
              or (:comment = true and r.comment is not null and r.comment != '')
              or (:comment = false and (r.comment is null or r.comment = '')))
          and (:image is null
              or (:image = true and r.etc is not null and r.etc != '[]')
              or (:image = false and (r.etc is null or r.etc = '[]')))
        """,
        nativeQuery = true,
    )
    fun countFilteredReviews(
        @Param("menuId") menuId: Long,
        @Param("comment") comment: Boolean?,
        @Param("image") image: Boolean?,
    ): Long

    @Query(
        value = """
        select comment
        from review_v2
        where score = :score and comment is not null and comment != ''
        order by rand()
        limit 1
        """,
        nativeQuery = true,
    )
    fun findRandomCommentByScore(
        @Param("score") score: Int,
    ): String?

    @Query(
        value = """
        select score, count(*) as cnt
        from review_v2 r
        where r.menu_id = :menuId
        group by score
        """,
        nativeQuery = true,
    )
    fun findScoreCountsByMenuId(
        @Param("menuId") menuId: Long,
    ): List<Array<Any>>

    @Query(
        value = """
        select count(distinct menu.restaurant_id)
        from review_v2 r
        join menu_v2 menu on menu.id = r.menu_id
        where r.user_id = :userId
        """,
        nativeQuery = true,
    )
    fun countDistinctRestaurantsByUserId(
        @Param("userId") userId: Int,
    ): Long

    @Query(
        value = """
        select menu.restaurant_id
        from review_v2 r
        join menu_v2 menu on menu.id = r.menu_id
        where r.user_id = :userId
        group by menu.restaurant_id
        order by max(r.created_at) desc, menu.restaurant_id asc
        """,
        nativeQuery = true,
    )
    fun findRestaurantIdsByUserIdPaged(
        @Param("userId") userId: Int,
        pageable: Pageable,
    ): List<Int>

    @Query(
        """
        select distinct r
        from review_v2 r
        join fetch r.menu menu
        join fetch menu.restaurant restaurant
        join fetch restaurant.building building
        join fetch r.user user
        where r.user.id = :userId
          and restaurant.id in (:restaurantIds)
        order by restaurant.id asc, r.createdAt desc
        """,
    )
    fun findAllByUserIdAndRestaurantIds(
        @Param("userId") userId: Int,
        @Param("restaurantIds") restaurantIds: List<Int>,
    ): List<ReviewV2>
}
