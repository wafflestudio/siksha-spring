package siksha.wafflestudio.core.domain.main.menu.repository

import org.springframework.data.jpa.repository.JpaRepository
import siksha.wafflestudio.core.domain.main.menu.data.MenuAliasV2

interface MenuAliasV2Repository : JpaRepository<MenuAliasV2, Long> {
    fun findByAlias(alias: String): MenuAliasV2?
}
