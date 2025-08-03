package siksha.wafflestudio.core.repository.menu

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.jdbc.Sql
import siksha.wafflestudio.core.domain.main.menu.data.Menu
import siksha.wafflestudio.core.domain.main.menu.repository.MenuRepository
import java.time.LocalDate

@DataJpaTest
@Sql("classpath:data/v001.sql")
class MenuRepositoryTest {
    @Autowired
    private lateinit var menuRepository: MenuRepository

    @Test
    fun `findAll should return all menus`() {
        // when
        val menus = menuRepository.findAll()

        // then
        assertThat(menus).hasSize(15)
    }

    @Test
    fun `findMenusByDate should return menus within the date range`() {
        // given
        val startDate = "2025-08-04"
        val endDate = "2025-08-05"

        // when
        val menus = menuRepository.findMenusByDate(startDate, endDate)

        // then
        assertThat(menus).hasSize(9)
        menus.forEach {
            assertThat(it.getDate()).isBetween(LocalDate.parse(startDate), LocalDate.parse(endDate))
        }
    }

    @Test
    fun `findMenuLikesByDateAndUserId should return menu like summaries`() {
        // given
        val userId = "1"
        val startDate = "2025-08-04"
        val endDate = "2025-08-04"

        // when
        val menuLikes = menuRepository.findMenuLikesByDateAndUserId(userId, startDate, endDate)

        // then
        assertThat(menuLikes).hasSize(2)
        val menu1Like = menuLikes.find { it.getId() == 1 }
        assertThat(menu1Like).isNotNull
        assertThat(menu1Like!!.getIsLiked()).isTrue
        assertThat(menu1Like.getLikeCnt()).isEqualTo(1)

        val menu2Like = menuLikes.find { it.getId() == 2 }
        assertThat(menu2Like).isNotNull
        assertThat(menu2Like!!.getIsLiked()).isTrue
        assertThat(menu2Like.getLikeCnt()).isEqualTo(1)
    }

    @Test
    fun `findMenuById should return a menu summary`() {
        // given
        val menuId = "1"

        // when
        val menu = menuRepository.findMenuById(menuId)

        // then
        assertThat(menu).isNotNull
        assertThat(menu.getId()).isEqualTo(1)
        assertThat(menu.getNameKr()).isEqualTo("돈까스")
        assertThat(menu.getReviewCnt()).isEqualTo(3)
        assertThat(menu.getScore()).isEqualTo(5.0)
    }

    @Test
    fun `findPlainMenuById should return a plain menu summary`() {
        // given
        val menuId = "1"

        // when
        val menu = menuRepository.findPlainMenuById(menuId)

        // then
        assertThat(menu).isNotNull
        assertThat(menu.getId()).isEqualTo(1)
        assertThat(menu.getRestaurantId()).isEqualTo(1)
        assertThat(menu.getCode()).isEqualTo("돈까스")
    }

    @Test
    fun `findMenuLikeByMenuId should return menu like count`() {
        // given
        val menuId = "1"

        // when
        val menuLikeCount = menuRepository.findMenuLikeByMenuId(menuId)

        // then
        assertThat(menuLikeCount).isNotNull
        assertThat(menuLikeCount.getId()).isEqualTo(1)
        assertThat(menuLikeCount.getLikeCount()).isEqualTo(1)
    }

    @Test
    fun `findMenuLikeByMenuIdAndUserId should return menu like summary for a user`() {
        // given
        val menuId = "1"
        val userId = "1"

        // when
        val menuLike = menuRepository.findMenuLikeByMenuIdAndUserId(menuId, userId)

        // then
        assertThat(menuLike).isNotNull
        assertThat(menuLike.getId()).isEqualTo(1)
        assertThat(menuLike.getLikeCnt()).isEqualTo(1)
        assertThat(menuLike.getIsLiked()).isTrue
    }

    @Test
    fun `findMenuLikeByMenuIdAndUserId should return correct isLiked when not liked`() {
        // given
        val menuId = "1"
        val userId = "2" // User 2 has not liked menu 1

        // when
        val menuLike = menuRepository.findMenuLikeByMenuIdAndUserId(menuId, userId)

        // then
        assertThat(menuLike).isNotNull
        assertThat(menuLike.getId()).isEqualTo(1)
        assertThat(menuLike.getLikeCnt()).isEqualTo(1)
        assertThat(menuLike.getIsLiked()).isFalse
    }

    @Test
    fun `save should persist a new menu`() {
        // given
        val restaurant = menuRepository.findById(1).get().restaurant

        // The Menu data class requires an ID for instantiation. We pass a dummy ID of 0,
        // which Spring Data JPA interprets as a new entity.
        val newMenu = Menu(
            id = 0,
            restaurant = restaurant,
            code = "새메뉴",
            date = LocalDate.now(),
            type = "LU",
            nameKr = "새로운 메뉴",
            nameEn = "New Menu",
            price = 10000,
            etc = "[]"
        )

        // when
        val savedMenu = menuRepository.save(newMenu)
        menuRepository.flush()

        // then
        assertThat(savedMenu.id).isNotEqualTo(0)
        val foundMenu = menuRepository.findById(savedMenu.id).orElse(null)
        assertThat(foundMenu).isNotNull
        assertThat(foundMenu!!.nameKr).isEqualTo("새로운 메뉴")
        assertThat(foundMenu.restaurant.id).isEqualTo(restaurant.id)
    }

    @Test
    fun `delete should remove a menu`() {
        // given
        val menuId = 1
        assertThat(menuRepository.findById(menuId)).isPresent

        // when
        menuRepository.deleteById(menuId)
        menuRepository.flush()

        // then
        assertThat(menuRepository.findById(menuId)).isNotPresent
    }
}
