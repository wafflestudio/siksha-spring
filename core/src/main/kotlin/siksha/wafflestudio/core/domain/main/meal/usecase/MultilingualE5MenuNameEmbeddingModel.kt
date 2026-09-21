package siksha.wafflestudio.core.domain.main.meal.usecase

import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory
import ai.djl.inference.Predictor
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

@Component
class MultilingualE5MenuNameEmbeddingModel(
    @param:Value("\${siksha.menu-normalizer.model-path:models/multilingual-e5-small}")
    private val modelPath: String,
    @param:Value("\${siksha.menu-normalizer.predictor-pool-size:2}")
    private val configuredPredictorPoolSize: Int,
    @param:Value("\${siksha.menu-normalizer.embedding-batch-size:64}")
    private val configuredBatchSize: Int,
) : MenuNameEmbeddingModel {
    @Volatile
    private var runtime: ModelRuntime? = null

    override val isReady: Boolean
        get() = runtime != null

    @PostConstruct
    fun init() {
        System.setProperty(DJL_OFFLINE_PROPERTY, "true")
        System.setProperty(DJL_OPT_OUT_TRACKING_PROPERTY, "true")

        val modelDirectory = runCatching { Path.of(modelPath).toAbsolutePath().normalize() }.getOrNull()
        if (modelDirectory == null || !hasRequiredModelFiles(modelDirectory)) {
            logger.warn("Pretrained menu name model not found or incomplete: {}", modelPath)
            return
        }

        try {
            val criteria =
                Criteria
                    .builder()
                    .setTypes(String::class.java, FloatArray::class.java)
                    .optModelPath(modelDirectory)
                    .optModelName(MODEL_NAME)
                    .optEngine(PYTORCH_ENGINE)
                    .optTranslatorFactory(TextEmbeddingTranslatorFactory())
                    .optArgument("maxLength", MAX_TOKEN_LENGTH)
                    .optArgument("padding", true)
                    .optOption("mapLocation", "true")
                    .build()
            val model = criteria.loadModel()

            try {
                val poolSize = configuredPredictorPoolSize.coerceIn(1, MAX_PREDICTOR_POOL_SIZE)
                val predictors = ArrayBlockingQueue<Predictor<String, FloatArray>>(poolSize)
                repeat(poolSize) { predictors.add(model.newPredictor()) }
                runtime = ModelRuntime(model, predictors)
                logger.info(
                    "Loaded pretrained menu name model from {} with {} predictors",
                    modelDirectory,
                    poolSize,
                )
            } catch (exception: Exception) {
                model.close()
                throw exception
            }
        } catch (exception: Exception) {
            logger.warn("Failed to load pretrained menu name model: {}", modelDirectory, exception)
        }
    }

    override fun embed(name: String): FloatArray? {
        if (name.isBlank()) return null

        return withPredictor { predictor ->
            normalize(predictor.predict(toModelInput(name)))
        }
    }

    override fun embedAll(names: List<String>): List<FloatArray?> {
        if (names.isEmpty()) return emptyList()
        if (!isReady) return List(names.size) { null }

        val batchSize = configuredBatchSize.coerceIn(1, MAX_EMBEDDING_BATCH_SIZE)
        return names.chunked(batchSize).flatMap { batch ->
            val embeddings =
                withPredictor { predictor ->
                    predictor.batchPredict(batch.map(::toModelInput)).map(::normalize)
                }
            embeddings ?: List(batch.size) { null }
        }
    }

    @PreDestroy
    fun close() {
        val currentRuntime = runtime ?: return
        runtime = null

        while (true) {
            val predictor = currentRuntime.predictors.poll() ?: break
            predictor.close()
        }
        currentRuntime.model.close()
    }

    private fun <T> withPredictor(block: (Predictor<String, FloatArray>) -> T): T? {
        val currentRuntime = runtime ?: return null
        var predictor: Predictor<String, FloatArray>? = null

        return try {
            predictor = currentRuntime.predictors.poll(PREDICTOR_WAIT_SECONDS, TimeUnit.SECONDS)
            if (predictor == null) {
                logger.warn("Timed out waiting for a menu name model predictor")
                null
            } else {
                block(predictor)
            }
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            null
        } catch (exception: Exception) {
            logger.warn("Failed to calculate a menu name embedding", exception)
            null
        } finally {
            predictor?.let { currentRuntime.predictors.offer(it) }
        }
    }

    private fun hasRequiredModelFiles(modelDirectory: Path): Boolean =
        Files.isDirectory(modelDirectory) &&
            Files.isRegularFile(modelDirectory.resolve("$MODEL_NAME.pt")) &&
            Files.isRegularFile(modelDirectory.resolve("tokenizer.json"))

    private fun toModelInput(name: String): String = "$SYMMETRIC_TASK_PREFIX$name"

    private fun normalize(vector: FloatArray): FloatArray? {
        val squaredNorm = vector.fold(0.0) { sum, value -> sum + value * value }
        if (!squaredNorm.isFinite() || squaredNorm <= 0.0) return null

        val norm = sqrt(squaredNorm).toFloat()
        return FloatArray(vector.size) { index -> vector[index] / norm }
    }

    private data class ModelRuntime(
        val model: ZooModel<String, FloatArray>,
        val predictors: ArrayBlockingQueue<Predictor<String, FloatArray>>,
    )

    private companion object {
        const val MODEL_NAME = "multilingual-e5-small"
        const val PYTORCH_ENGINE = "PyTorch"
        const val SYMMETRIC_TASK_PREFIX = "query: "
        const val MAX_TOKEN_LENGTH = 64
        const val MAX_PREDICTOR_POOL_SIZE = 8
        const val MAX_EMBEDDING_BATCH_SIZE = 256
        const val PREDICTOR_WAIT_SECONDS = 10L
        const val DJL_OFFLINE_PROPERTY = "ai.djl.offline"
        const val DJL_OPT_OUT_TRACKING_PROPERTY = "OPT_OUT_TRACKING"
        val logger = LoggerFactory.getLogger(MultilingualE5MenuNameEmbeddingModel::class.java)
    }
}
