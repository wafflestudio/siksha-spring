package siksha.wafflestudio.core.domain.main.menu.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.menu.data.MenuV2
import siksha.wafflestudio.core.domain.main.restaurant.data.CornerV2

interface MenuV2Repository : JpaRepository<MenuV2, Long> {
    fun findByCornerAndName(
        corner: CornerV2,
        name: String,
    ): MenuV2?
}
