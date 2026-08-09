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
    private val menuNameNormalizer: MenuNameNormalizer,
) {
    operator fun invoke(
        originalName: String,
        restaurant: RestaurantV2,
    ): MenuV2 {
        val alias = menuAliasV2Repository.findByAlias(originalName)
        if (alias != null) return findOrCreateMenu(restaurant, alias.menuName)

        val preprocessedName = MenuNamePreprocessor.preprocess(originalName)
        val preprocessedMenu = menuV2Repository.findByRestaurantAndName(restaurant, preprocessedName)
        val normalizedName =
            preprocessedMenu?.name
                ?: menuNameNormalizer
                    .normalize(preprocessedName)
                    .takeIf { it.confidence > NORMALIZATION_CONFIDENCE_THRESHOLD }
                    ?.normalizedName
                ?: preprocessedName

        val normalizedMenu =
            if (normalizedName == preprocessedName) {
                null
            } else {
                menuV2Repository.findByRestaurantAndName(restaurant, normalizedName)
            }
        val menu =
            preprocessedMenu ?: normalizedMenu
                ?: menuV2Repository.save(
                    MenuV2(
                        restaurant = restaurant,
                        name = normalizedName,
                    ),
                )

        if (originalName != menu.name) {
            val savedAlias =
                menuAliasV2Repository.save(
                    MenuAliasV2(
                        alias = originalName,
                        menuName = menu.name,
                    ),
                )
            menuNameNormalizer.addAlias(savedAlias.alias, savedAlias.menuName)
        }

        return menu
    }

    private fun findOrCreateMenu(
        restaurant: RestaurantV2,
        name: String,
    ): MenuV2 =
        menuV2Repository.findByRestaurantAndName(restaurant, name)
            ?: menuV2Repository.save(
                MenuV2(
                    restaurant = restaurant,
                    name = name,
                ),
            )

    companion object {
        private const val NORMALIZATION_CONFIDENCE_THRESHOLD = 0.85
    }
}
