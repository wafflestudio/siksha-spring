package siksha.wafflestudio.core.util

object KeywordReviewUtil {
    private val tasteGrade = listOf("별로예요", "아쉬운 맛이에요", "무난해요", "생각보다 맛있어요", "또 먹고 싶어요")
    private val priceGrade = listOf("너무 비싸요", "약간 비싸요", "합리적이에요", "가성비 좋아요", "혜자스러워요")
    private val foodCompositionGrade = listOf("너무 빈약해요", "다소 단조로워요", "기본적이에요", "알찬 편이에요", "조화로워요")

    fun getTasteKeyword(tasteLevel: Int?): String {
        if (tasteLevel == null || tasteLevel == -1 || tasteLevel > 4) return ""
        return tasteGrade[tasteLevel]
    }

    fun getPriceKeyword(priceLevel: Int?): String {
        if (priceLevel == null || priceLevel == -1 || priceLevel > 4) return ""
        return priceGrade[priceLevel]
    }

    fun getFoodCompositionKeyword(foodLevel: Int?): String {
        if (foodLevel == null || foodLevel == -1 || foodLevel > 4) return ""
        return foodCompositionGrade[foodLevel]
    }

    fun getTasteLevel(tasteKeyword: String?): Int {
        if (tasteKeyword == null || !tasteGrade.contains(tasteKeyword)) return -1
        return tasteGrade.indexOf(tasteKeyword)
    }

    fun getPriceLevel(priceKeyword: String?): Int {
        if (priceKeyword == null || !priceGrade.contains(priceKeyword)) return -1
        return priceGrade.indexOf(priceKeyword)
    }

    fun getFoodCompositionLevel(foodKeyword: String?): Int {
        if (foodKeyword == null || !foodCompositionGrade.contains(foodKeyword)) return -1
        return foodCompositionGrade.indexOf(foodKeyword)
    }
}
