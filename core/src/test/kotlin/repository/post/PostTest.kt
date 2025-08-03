package siksha.wafflestudio.core.repository.post

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.jdbc.Sql
import siksha.wafflestudio.core.domain.community.post.repository.PostRepository
import java.time.OffsetDateTime
import java.time.ZoneOffset

@DataJpaTest
@Sql("classpath:data/v001.sql")
class PostTest {

    @Autowired
    private lateinit var repository: PostRepository

    @Test
    fun `findAll should return all posts`() {
        // when
        val posts = repository.findAll()

        // then
        assertThat(posts).hasSize(10)
    }

    @Test
    fun `findPageByBoardId should return posts for board 1 ordered desc by createdAt`() {
        // when
        val page = repository.findPageByBoardId(1, PageRequest.of(0, 10))

        // then
        assertThat(page.content).hasSize(5)
        // 생성일 내림차순: IDs 10,4,3,2,1
        assertThat(page.content.map { it.id }).containsExactly(10, 4, 3, 2, 1)
    }

    @Test
    fun `findPageByUserId should return posts for user 1 ordered desc by createdAt`() {
        // when
        val page = repository.findPageByUserId(1, PageRequest.of(0, 10))

        // then
        assertThat(page.content).hasSize(2)
        // user 1의 포스트 IDs 6(2025-08-04 13:00), 1(2024-09-10 23:16)
        assertThat(page.content.map { it.id }).containsExactly(6, 1)
    }

    @Test
    fun `findTrending should return top 5 posts created on or after 2025-08-04 with at least 1 like`() {
        // given: Asia/Seoul 기준 2025-08-04 00:00
        val createdDays = OffsetDateTime.of(2025, 8, 4, 0, 0, 0, 0, ZoneOffset.ofHours(9))

        // when
        val page = repository.findTrending(1, createdDays)

        // then
        assertThat(page.content).hasSize(5)
        // 모두 좋아요 1개씩, 생성일 내림차순으로 IDs 10,9,8,7,6
        assertThat(page.content.map { it.id }).containsExactly(10, 9, 8, 7, 6)
    }

    @Test
    fun `findBest should return top 5 posts by like count`() {
        // when
        val page = repository.findBest(1, PageRequest.of(0, 5))

        // then
        assertThat(page.content).hasSize(5)
        // 모든 포스트 좋아요 1개씩, 생성일 내림차순으로 IDs 10,9,8,7,6
        assertThat(page.content.map { it.id }).containsExactly(10, 9, 8, 7, 6)
    }
}
