package siksha.wafflestudio.core.domain.main.meal.usecase

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory
import java.io.File

object TrainMenuWord2Vec {
    @JvmStatic
    fun main(args: Array<String>) {
        val pairFile = File(args.getOrElse(0) { DEFAULT_PAIR_FILE })
        val modelFile = File(args.getOrElse(1) { DEFAULT_MODEL_FILE })

        require(pairFile.isFile) {
            "Menu normalizer training pair file does not exist: ${pairFile.path}"
        }

        modelFile.parentFile?.mkdirs()
        val corpusFile = modelFile.resolveSibling("${modelFile.nameWithoutExtension}-corpus.txt")
        writeCorpus(pairFile, corpusFile)

        val model =
            Word2Vec
                .Builder()
                .minWordFrequency(1)
                .layerSize(100)
                .windowSize(8)
                .seed(42)
                .epochs(30)
                .iterations(1)
                .workers(Runtime.getRuntime().availableProcessors().coerceAtMost(4))
                .iterate(BasicLineIterator(corpusFile))
                .tokenizerFactory(DefaultTokenizerFactory())
                .build()

        model.fit()
        WordVectorSerializer.writeWord2VecModel(model, modelFile)
    }

    private fun writeCorpus(
        pairFile: File,
        corpusFile: File,
    ) {
        corpusFile.bufferedWriter(Charsets.UTF_8).use { writer ->
            pairFile.useLines(Charsets.UTF_8) { lines ->
                lines
                    .map { it.trim() }
                    .filter { it.isNotBlank() && !it.startsWith("#") }
                    .mapNotNull(::parsePair)
                    .forEach { pair ->
                        writer.appendLine(corpusLine(pair.originalName, pair.normalizedName))
                    }
            }
        }
    }

    private fun parsePair(line: String): MenuNamePair? {
        val columns = line.split('\t').map { it.trim() }
        val originalName = columns.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return null
        val normalizedName = columns.getOrNull(1)?.takeIf { it.isNotBlank() } ?: originalName

        return MenuNamePair(originalName, normalizedName)
    }

    private fun corpusLine(
        originalName: String,
        normalizedName: String,
    ): String {
        val preprocessedOriginalName = MenuNamePreprocessor.preprocess(originalName)
        val preprocessedNormalizedName = MenuNamePreprocessor.preprocess(normalizedName)
        val originalTokens = MenuNameTokenizer.tokenize(preprocessedOriginalName)
        val normalizedTokens = MenuNameTokenizer.tokenize(preprocessedNormalizedName)

        return (originalTokens + normalizedTokens + normalizedTokens).joinToString(" ")
    }

    private data class MenuNamePair(
        val originalName: String,
        val normalizedName: String,
    )

    private const val DEFAULT_PAIR_FILE = "data/menu-normalizer-pairs.tsv"
    private const val DEFAULT_MODEL_FILE = "models/menu-word2vec.bin"
}
