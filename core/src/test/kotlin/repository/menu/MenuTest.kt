package siksha.wafflestudio.core.repository.menu

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import siksha.wafflestudio.core.domain.main.menu.repository.MenuRepository

@SpringBootTest
class MenuTest {
    @Autowired
    lateinit var repository: MenuRepository

    @Test
    fun testMenu() {
        val menus = repository.findAll()
        assert(menus.isNotEmpty())
    }
}


