package siksha.wafflestudio.core.usecase.meal

import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import siksha.wafflestudio.core.domain.main.meal.usecase.MultilingualE5MenuNameEmbeddingModel
import java.nio.file.Path
import kotlin.math.sqrt
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MultilingualE5MenuNameEmbeddingModelTest {
    @TempDir
    lateinit var emptyModelDirectory: Path

    @Test
    fun `모델 파일이 없으면 안전하게 비활성화된다`() {
        val model = MultilingualE5MenuNameEmbeddingModel(emptyModelDirectory.toString(), 1, 8)

        model.init()

        assertFalse(model.isReady)
        assertNull(model.embed("치즈돈까스"))
    }

    @Test
    fun `실제 multilingual E5 모델로 한국어 메뉴명을 임베딩한다`() {
        val modelPath = System.getenv(MODEL_PATH_ENV)
        assumeTrue(!modelPath.isNullOrBlank(), "$MODEL_PATH_ENV is required for the pretrained model smoke test")
        val model = MultilingualE5MenuNameEmbeddingModel(modelPath, 1, 8)

        try {
            model.init()
            assertTrue(model.isReady)

            val cheesePorkCutlet = assertNotNull(model.embed("치즈돈까스"))
            val cheesePorkCutletVariant = assertNotNull(model.embed("치즈 돈가스"))
            val icedAmericano = assertNotNull(model.embed("아이스 아메리카노"))
            val variantSimilarity = cosine(cheesePorkCutlet, cheesePorkCutletVariant)
            val unrelatedSimilarity = cosine(cheesePorkCutlet, icedAmericano)

            assertEquals(384, cheesePorkCutlet.size)
            assertTrue(kotlin.math.abs(norm(cheesePorkCutlet) - 1.0) < 0.0001)
            assertTrue(variantSimilarity > unrelatedSimilarity)
        } finally {
            model.close()
        }
    }

    private fun norm(vector: FloatArray): Double = sqrt(vector.fold(0.0) { sum, value -> sum + value * value })

    private fun cosine(
        a: FloatArray,
        b: FloatArray,
    ): Double = a.indices.sumOf { index -> (a[index] * b[index]).toDouble() }

    private companion object {
        const val MODEL_PATH_ENV = "SIKSHA_TEST_MENU_NORMALIZER_MODEL_PATH"
    }
}
