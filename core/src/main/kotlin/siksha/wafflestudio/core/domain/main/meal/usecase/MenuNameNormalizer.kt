package siksha.wafflestudio.core.domain.main.meal.usecase

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAliasV2Repository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuV2Repository

@Component
class MenuNameNormalizer(
    private val menuAliasV2Repository: MenuAliasV2Repository,
    private val menuV2Repository: MenuV2Repository,
    private val embeddingModel: MenuNameEmbeddingModel,
) {
    @Volatile
    private var aliasVectors: List<AliasVector> = emptyList()

    private val aliasUpdateLock = Any()

    @PostConstruct
    fun init() {
        if (!embeddingModel.isReady) {
            logger.warn("Menu name normalization will use rule-based fallback because the pretrained model is unavailable")
            return
        }

        runCatching { rebuildAliasVectors() }
            .onFailure { logger.warn("Failed to build the menu alias vector index", it) }
    }

    fun normalize(preprocessedName: String): NormalizationResult {
        val inputVector =
            embeddingModel.embed(preprocessedName)
                ?: return NormalizationResult(preprocessedName, 0.0)

        val best =
            aliasVectors
                .asSequence()
                .map { aliasVector ->
                    val semanticSimilarity = cosine(inputVector, aliasVector.vector).coerceIn(0.0, 1.0)
                    val lexicalSimilarity =
                        normalizedEditSimilarity(
                            preprocessedName,
                            aliasVector.embeddingText,
                        )
                    aliasVector to
                        SEMANTIC_SIMILARITY_WEIGHT * semanticSimilarity +
                        LEXICAL_SIMILARITY_WEIGHT * lexicalSimilarity
                }.maxByOrNull { it.second }
                ?: return NormalizationResult(preprocessedName, 0.0)

        return NormalizationResult(
            normalizedName = best.first.menuName,
            confidence = best.second,
        )
    }

    fun addAlias(
        alias: String,
        menuName: String,
    ) {
        val vector = embeddingModel.embed(MenuNamePreprocessor.preprocess(alias)) ?: return
        synchronized(aliasUpdateLock) {
            aliasVectors =
                aliasVectors.filterNot { it.alias == alias } +
                AliasVector(
                    alias = alias,
                    embeddingText = MenuNamePreprocessor.preprocess(alias),
                    menuName = menuName,
                    vector = vector,
                )
        }
    }

    private fun rebuildAliasVectors() {
        val candidates =
            buildList {
                menuAliasV2Repository.findAll().forEach { alias ->
                    add(
                        AliasCandidate(
                            alias = alias.alias,
                            embeddingText = MenuNamePreprocessor.preprocess(alias.alias),
                            menuName = alias.menuName,
                        ),
                    )
                }
                menuV2Repository.findDistinctNames().forEach { menuName ->
                    add(
                        AliasCandidate(
                            alias = menuName,
                            embeddingText = MenuNamePreprocessor.preprocess(menuName),
                            menuName = menuName,
                        ),
                    )
                }
            }.distinctBy { it.embeddingText to it.menuName }
        val embeddings = embeddingModel.embedAll(candidates.map(AliasCandidate::embeddingText))

        aliasVectors =
            candidates.zip(embeddings).mapNotNull { (candidate, vector) ->
                vector?.let {
                    AliasVector(
                        alias = candidate.alias,
                        embeddingText = candidate.embeddingText,
                        menuName = candidate.menuName,
                        vector = it,
                    )
                }
            }

        logger.info(
            "Built menu alias vector index with {} of {} candidates",
            aliasVectors.size,
            candidates.size,
        )
    }

    private fun cosine(
        a: FloatArray,
        b: FloatArray,
    ): Double {
        if (a.size != b.size || a.isEmpty()) return -1.0

        var dotProduct = 0.0
        var aSquaredNorm = 0.0
        var bSquaredNorm = 0.0
        for (index in a.indices) {
            dotProduct += a[index] * b[index]
            aSquaredNorm += a[index] * a[index]
            bSquaredNorm += b[index] * b[index]
        }
        if (aSquaredNorm == 0.0 || bSquaredNorm == 0.0) return -1.0

        return (dotProduct / kotlin.math.sqrt(aSquaredNorm * bSquaredNorm)).coerceIn(-1.0, 1.0)
    }

    private fun normalizedEditSimilarity(
        left: String,
        right: String,
    ): Double {
        if (left == right) return 1.0
        val maxLength = maxOf(left.length, right.length)
        if (maxLength == 0) return 1.0

        var previous = IntArray(right.length + 1) { it }
        var current = IntArray(right.length + 1)
        for (leftIndex in left.indices) {
            current[0] = leftIndex + 1
            for (rightIndex in right.indices) {
                val substitutionCost = if (left[leftIndex] == right[rightIndex]) 0 else 1
                current[rightIndex + 1] =
                    minOf(
                        current[rightIndex] + 1,
                        previous[rightIndex + 1] + 1,
                        previous[rightIndex] + substitutionCost,
                    )
            }
            val swap = previous
            previous = current
            current = swap
        }

        return 1.0 - previous[right.length].toDouble() / maxLength
    }

    data class NormalizationResult(
        val normalizedName: String,
        val confidence: Double,
    )

    private data class AliasCandidate(
        val alias: String,
        val embeddingText: String,
        val menuName: String,
    )

    private data class AliasVector(
        val alias: String,
        val embeddingText: String,
        val menuName: String,
        val vector: FloatArray,
    )

    private companion object {
        const val SEMANTIC_SIMILARITY_WEIGHT = 0.75
        const val LEXICAL_SIMILARITY_WEIGHT = 0.25
        val logger = LoggerFactory.getLogger(MenuNameNormalizer::class.java)
    }
}
