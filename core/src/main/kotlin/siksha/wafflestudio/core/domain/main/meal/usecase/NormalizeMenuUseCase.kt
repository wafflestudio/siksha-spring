package siksha.wafflestudio.core.domain.main.meal.usecase

import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.main.menu.data.MenuV2
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAliasV2Repository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuV2Repository
import siksha.wafflestudio.core.domain.main.restaurant.data.RestaurantV2

/**
 * 크롤링 원본 메뉴명을 정규화된 MenuV2 entity로 변환
 * 현재는 stub 구현으로, alias 매핑이 있으면 사용하고 없으면 원본명 그대로 사용
 * 추후 메뉴명 정규화 로직(LLM, fuzzy matching 등)이 이 UseCase 내부에서 구현될 예정
 */
@Component
class NormalizeMenuUseCase(
    private val menuAliasV2Repository: MenuAliasV2Repository,
    private val menuV2Repository: MenuV2Repository,
) {
    operator fun invoke(
        originalName: String,
        restaurant: RestaurantV2,
    ): MenuV2 {
        val normalizedName = menuAliasV2Repository.findByAlias(originalName)?.menuName ?: originalName

        return menuV2Repository.findByRestaurantAndName(restaurant, normalizedName)
            ?: menuV2Repository.save(
                MenuV2(
                    restaurant = restaurant,
                    name = normalizedName,
                ),
            )
    }
}
