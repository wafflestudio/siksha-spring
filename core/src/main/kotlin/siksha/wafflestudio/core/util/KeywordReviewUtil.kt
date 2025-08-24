package siksha.wafflestudio.core.util

object KeywordReviewUtil {
    private val flavorGrade = listOf("별로예요", "아쉬운 맛이에요", "무난해요", "생각보다 맛있어요", "또 먹고 싶어요")
    private val priceGrade = listOf("너무 비싸요", "약간 비싸요", "합리적이에요", "가성비 좋아요", "혜자스러워요")
    private val foodCompositionGrade = listOf("너무 빈약해요", "다소 단조로워요", "기본적이에요", "알찬 편이에요", "조화로워요")

    fun getFlavorKeyword(flavorLevel: Int?): String {
        if (flavorLevel == null || flavorLevel == -1) return ""
        return flavorGrade[flavorLevel]
    }

    fun getPriceKeyword(priceLevel: Int?): String {
        if (priceLevel == null || priceLevel == -1) return ""
        return priceGrade[priceLevel]
    }

    fun getFoodCompositionKeyword(foodLevel: Int?): String {
        if (foodLevel == null || foodLevel == -1) return ""
        return foodCompositionGrade[foodLevel]
    }

    fun getFlavorLevel(flavorKeyword: String?): Int {
        if (flavorKeyword == null) return -1
        return flavorGrade.indexOf(flavorKeyword)
    }

    fun getPriceLevel(priceKeyword: String?): Int {
        if (priceKeyword == null) return -1
        return priceGrade.indexOf(priceKeyword)
    }

    fun getFoodCompositionLevel(foodKeyword: String?): Int {
        if (foodKeyword == null) return -1
        return foodCompositionGrade.indexOf(foodKeyword)
    }
}
