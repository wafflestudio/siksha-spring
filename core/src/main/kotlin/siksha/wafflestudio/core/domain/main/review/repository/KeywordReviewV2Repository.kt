package siksha.wafflestudio.core.domain.main.review.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import siksha.wafflestudio.core.domain.main.review.data.KeywordReviewV2
import siksha.wafflestudio.core.domain.main.review.dto.KeywordReviewV2Summary

interface KeywordReviewV2Repository : JpaRepository<KeywordReviewV2, Long> {
    @Query(
        value = """
        select
            taste_stats.taste_keyword as tasteKeyword,
            taste_stats.taste_cnt as tasteCnt,
            totals.taste_total as tasteTotal,
            price_stats.price_keyword as priceKeyword,
            price_stats.price_cnt as priceCnt,
            totals.price_total as priceTotal,
            food_stats.food_composition_keyword as foodCompositionKeyword,
            food_stats.food_composition_cnt as foodCompositionCnt,
            totals.food_composition_total as foodCompositionTotal
        from (select 1) dummy
        left join (
            select kr.taste as taste_keyword, count(*) as taste_cnt
            from keyword_review_v2 kr
            join review_v2 r on r.id = kr.review_id
            where r.menu_id = :menuId and kr.taste != -1
            group by kr.taste
            order by taste_cnt desc
            limit 1
        ) taste_stats on 1 = 1
        left join (
            select kr.price as price_keyword, count(*) as price_cnt
            from keyword_review_v2 kr
            join review_v2 r on r.id = kr.review_id
            where r.menu_id = :menuId and kr.price != -1
            group by kr.price
            order by price_cnt desc
            limit 1
        ) price_stats on 1 = 1
        left join (
            select kr.food_composition as food_composition_keyword, count(*) as food_composition_cnt
            from keyword_review_v2 kr
            join review_v2 r on r.id = kr.review_id
            where r.menu_id = :menuId and kr.food_composition != -1
            group by kr.food_composition
            order by food_composition_cnt desc
            limit 1
        ) food_stats on 1 = 1
        left join (
            select
                count(case when kr.taste != -1 then 1 end) as taste_total,
                count(case when kr.price != -1 then 1 end) as price_total,
                count(case when kr.food_composition != -1 then 1 end) as food_composition_total
            from keyword_review_v2 kr
            join review_v2 r on r.id = kr.review_id
            where r.menu_id = :menuId
        ) totals on 1 = 1
        """,
        nativeQuery = true,
    )
    fun findScoreCountsByMenuId(
        @Param("menuId") menuId: Long,
    ): KeywordReviewV2Summary
}
