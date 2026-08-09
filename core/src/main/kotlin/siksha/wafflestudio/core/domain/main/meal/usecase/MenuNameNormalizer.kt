package siksha.wafflestudio.core.domain.main.meal.usecase

import jakarta.annotation.PostConstruct
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import org.nd4j.linalg.api.ndarray.INDArray
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.main.menu.data.MenuAliasV2
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAliasV2Repository
import java.io.File

@Component
class MenuNameNormalizer(
    private val menuAliasV2Repository: MenuAliasV2Repository,
    @param:Value("\${siksha.menu-normalizer.model-path:models/menu-word2vec.bin}")
    private val modelPath: String,
) {
    private var wordVectors: WordVectors? = null

    @Volatile
    private var aliasVectors: List<AliasVector> = emptyList()

    @PostConstruct
    fun init() {
        val modelFile = File(modelPath)
        if (!modelFile.isFile) {
            logger.warn("Menu name normalizer model file not found: {}", modelFile.path)
            return
        }

        wordVectors =
            runCatching { WordVectorSerializer.readWord2VecModel(modelFile) }
                .onFailure { logger.warn("Failed to load menu name normalizer model: {}", modelFile.path, it) }
                .getOrNull()

        rebuildAliasVectors(menuAliasV2Repository.findAll())
    }

    fun normalize(preprocessedName: String): NormalizationResult {
        val inputVector =
            embed(preprocessedName)
                ?: return NormalizationResult(preprocessedName, 0.0)

        val best =
            aliasVectors
                .asSequence()
                .map { aliasVector -> aliasVector to cosine(inputVector, aliasVector.vector) }
                .maxByOrNull { it.second }
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
        val vector = embed(MenuNamePreprocessor.preprocess(alias)) ?: return
        aliasVectors =
            aliasVectors +
            AliasVector(
                alias = alias,
                menuName = menuName,
                vector = vector,
            )
    }

    private fun embed(name: String): INDArray? {
        val currentWordVectors = wordVectors ?: return null
        val tokens = MenuNameTokenizer.tokenize(name).filter { currentWordVectors.hasWord(it) }
        if (tokens.isEmpty()) return null

        val vector = currentWordVectors.getWordVectorsMean(tokens)
        val norm = vector.norm2Number().toDouble()

        return if (norm == 0.0) null else vector.div(norm)
    }

    private fun rebuildAliasVectors(aliases: List<MenuAliasV2>) {
        aliasVectors =
            aliases.mapNotNull { alias ->
                embed(MenuNamePreprocessor.preprocess(alias.alias))?.let { vector ->
                    AliasVector(
                        alias = alias.alias,
                        menuName = alias.menuName,
                        vector = vector,
                    )
                }
            }
    }

    private fun cosine(
        a: INDArray,
        b: INDArray,
    ): Double =
        a.mul(b).sumNumber().toDouble() /
            (a.norm2Number().toDouble() * b.norm2Number().toDouble())

    data class NormalizationResult(
        val normalizedName: String,
        val confidence: Double,
    )

    private data class AliasVector(
        val alias: String,
        val menuName: String,
        val vector: INDArray,
    )

    companion object {
        private val logger = LoggerFactory.getLogger(MenuNameNormalizer::class.java)
    }
}
