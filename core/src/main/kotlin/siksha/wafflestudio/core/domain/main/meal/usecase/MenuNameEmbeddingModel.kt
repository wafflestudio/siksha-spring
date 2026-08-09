package siksha.wafflestudio.core.domain.main.meal.usecase

interface MenuNameEmbeddingModel {
    val isReady: Boolean

    fun embed(name: String): FloatArray?

    fun embedAll(names: List<String>): List<FloatArray?> = names.map(::embed)
}
