package siksha.wafflestudio.core.repository.review

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.jdbc.Sql
import siksha.wafflestudio.core.domain.main.review.repository.ReviewRepository

@DataJpaTest
@Sql("classpath:data/v001.sql")
class ReviewRepositoryTest {

    @Autowired
    private lateinit var repository: ReviewRepository

    @Test
    fun `findByMenuIdOrderByCreatedAtDesc should return reviews for menu 1 ordered desc`() {
        // when
        val reviews = repository.findByMenuIdOrderByCreatedAtDesc(1, PageRequest.of(0, 10))

        // then
        assertThat(reviews).hasSize(3)
        assertThat(reviews.map { it.id }).containsExactly(11, 6, 1)
    }

    @Test
    fun `countByMenuId should return correct count`() {
        // when
        val count = repository.countByMenuId(1)

        // then
        assertThat(count).isEqualTo(3)
    }

    @Test
    fun `findFilteredReviews should apply comment and etc filters`() {
        // all reviews for menu 1
        val all = repository.findFilteredReviews(1, null, null, 10, 0)
        assertThat(all).hasSize(3)

        // only with comment (all have non-null comment)
        val withComment = repository.findFilteredReviews(1, true, null, 10, 0)
        assertThat(withComment).hasSize(3)

        // only with etc.images (none have images)
        val withImages = repository.findFilteredReviews(1, null, true, 10, 0)
        assertThat(withImages).isEmpty()

        // comment=false should yield none
        val noComment = repository.findFilteredReviews(1, false, null, 10, 0)
        assertThat(noComment).isEmpty()
    }

    @Test
    fun `countFilteredReviews should apply same filters`() {
        // no filters => count all
        assertThat(repository.countFilteredReviews(1, null, null)).isEqualTo(3)
        // only comment
        assertThat(repository.countFilteredReviews(1, true, null)).isEqualTo(3)
        // only images
        assertThat(repository.countFilteredReviews(1, null, true)).isEqualTo(0)
        // comment=false
        assertThat(repository.countFilteredReviews(1, false, null)).isEqualTo(0)
    }

    @Test
    fun `findByMenuIdAndUserId should return a review or null`() {
        // exists
        val r1 = repository.findByMenuIdAndUserId(1, 1)
        assertThat(r1).isNotNull
        assertThat(r1!!.menu.id).isEqualTo(1)
        assertThat(r1.user.id).isEqualTo(1)

        // non-existing
        val r2 = repository.findByMenuIdAndUserId(2, 1)
        assertThat(r2).isNull()
    }

    @Test
    fun `findByUserId and countByUserId should return correct page and count`() {
        val page = repository.findByUserId(1, 2, 0)
        assertThat(page).hasSize(2)
        assertThat(page.map { it.id }).containsExactly(11, 6)

        val total = repository.countByUserId(1)
        assertThat(total).isEqualTo(3)
    }

    @Test
    fun `findRandomCommentByScore should return a comment for score 5`() {
        val comment = repository.findRandomCommentByScore(5)
        val expected = setOf("맛있어요!", "JMT", "Perfect", "I love it", "Excellent!")
        assertThat(comment).isIn(expected)

        // no such score
        val none = repository.findRandomCommentByScore(6)
        assertThat(none).isNull()
    }

    @Test
    fun `findScoreCountsByMenuId should return score group counts`() {
        val counts = repository.findScoreCountsByMenuId(1)
        assertThat(counts).hasSize(1)

        val entry = counts[0]
        val score = (entry[0] as Number).toInt()
        val cnt = (entry[1] as Number).toLong()

        assertThat(score).isEqualTo(5)
        assertThat(cnt).isEqualTo(3)
    }
}
