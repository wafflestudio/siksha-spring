package siksha.wafflestudio.core.domain.main.meal.usecase

import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.main.menu.data.MenuAliasV2
import siksha.wafflestudio.core.domain.main.menu.data.MenuV2
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAliasV2Repository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2

@Component
class NormalizeMenuUseCase(
    private val menuAliasV2Repository: MenuAliasV2Repository,
    private val menuV2Repository: MenuV2Repository,
) {
    operator fun invoke(
        originalName: String,
        restaurant: RestaurantV2,
    ): MenuV2 {
        val alias = menuAliasV2Repository.findByAlias(originalName)
        val normalizedName = alias?.menuName ?: normalizeName(originalName)

        val menu =
            menuV2Repository.findByRestaurantAndName(restaurant, normalizedName)
                ?: menuV2Repository.save(
                    MenuV2(
                        restaurant = restaurant,
                        name = normalizedName,
                    ),
                )

        if (alias == null && originalName != menu.name) {
            menuAliasV2Repository.save(
                MenuAliasV2(
                    alias = originalName,
                    menuName = menu.name,
                ),
            )
        }

        return menu
    }

    private fun normalizeName(name: String): String {
        var normalized = name.trim()
        normalized = normalized.replace(OPERATING_PARENTHESIS_REGEX, " ")
        normalized = normalized.replace(DELIMITER_REGEX, " ")
        normalized = normalized.replace(OPERATING_TOKEN_REGEX, " ")
        normalized = normalized.replace(DESCRIPTIVE_TOKEN_REGEX, " ")
        normalized = normalized.replace(MULTIPLE_SPACES_REGEX, " ").trim()
        normalized = normalized.replace(SPACES_REGEX, "")

        return normalized.ifBlank { name.trim() }
    }

    companion object {
        private val OPERATING_PARENTHESIS_REGEX = Regex("""\((HOT|NEW|추천|특식|한정|셀프|추가|별도)[^)]*\)""")
        private val DELIMITER_REGEX = Regex("""[\/,|]+""")
        private val OPERATING_TOKEN_REGEX = Regex("""\b(HOT|NEW|추천|특식|한정|셀프|추가|별도)\b""")
        private val DESCRIPTIVE_TOKEN_REGEX =
            Regex("""(원산지[^\s]*|알레르기[^\s]*|소스\s*별도|밥\s*/?\s*김치\s*포함|밥\s*포함|김치\s*포함)""")
        private val MULTIPLE_SPACES_REGEX = Regex("""\s+""")
        private val SPACES_REGEX = Regex("""\s+""")
    }
}
