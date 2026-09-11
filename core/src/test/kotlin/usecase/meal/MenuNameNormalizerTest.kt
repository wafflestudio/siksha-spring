package siksha.wafflestudio.core.usecase.meal

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.main.meal.usecase.MenuNameEmbeddingModel
import siksha.wafflestudio.core.domain.main.meal.usecase.MenuNameNormalizer
import siksha.wafflestudio.core.domain.main.menu.data.MenuAliasV2
import siksha.wafflestudio.core.domain.main.menu.repository.MenuAliasV2Repository
import siksha.wafflestudio.core.domain.main.menu.repository.MenuV2Repository
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MenuNameNormalizerTest {
    private val menuAliasV2Repository = mockk<MenuAliasV2Repository>()
    private val menuV2Repository = mockk<MenuV2Repository>()

    @Test
    fun `사전 학습 모델 임베딩과 가장 유사한 alias의 메뉴명을 반환한다`() {
        val embeddingModel =
            FakeEmbeddingModel(
                mapOf(
                    "치즈돈까스" to floatArrayOf(1.0f, 0.0f),
                    "비빔밥" to floatArrayOf(0.0f, 1.0f),
                    "치즈돈가스" to floatArrayOf(0.95f, 0.05f),
                ),
            )
        every { menuAliasV2Repository.findAll() } returns
            listOf(MenuAliasV2(id = 1, alias = "치즈 돈까스", menuName = "치즈돈까스"))
        every { menuV2Repository.findDistinctNames() } returns listOf("비빔밥")
        val normalizer = MenuNameNormalizer(menuAliasV2Repository, menuV2Repository, embeddingModel)

        normalizer.init()
        val result = normalizer.normalize("치즈돈가스")

        assertEquals("치즈돈까스", result.normalizedName)
        assertTrue(result.confidence > 0.90)
    }

    @Test
    fun `새 alias를 저장하면 재시작 없이 벡터 인덱스에 반영한다`() {
        val embeddingModel =
            FakeEmbeddingModel(
                mapOf(
                    "비빔밥" to floatArrayOf(0.0f, 1.0f),
                ),
            )
        every { menuAliasV2Repository.findAll() } returns emptyList()
        every { menuV2Repository.findDistinctNames() } returns emptyList()
        val normalizer = MenuNameNormalizer(menuAliasV2Repository, menuV2Repository, embeddingModel)
        normalizer.init()

        normalizer.addAlias("비빔밥", "비빔밥")
        val result = normalizer.normalize("비빔밥")

        assertEquals("비빔밥", result.normalizedName)
        assertTrue(result.confidence > 0.90)
    }

    @Test
    fun `의미상 비슷해도 수식어가 다른 메뉴는 confidence 기준을 넘지 않는다`() {
        val embeddingModel =
            FakeEmbeddingModel(
                mapOf(
                    "치즈돈까스" to floatArrayOf(1.0f, 0.0f),
                    "매운돈까스" to floatArrayOf(0.99f, 0.01f),
                ),
            )
        every { menuAliasV2Repository.findAll() } returns
            listOf(MenuAliasV2(id = 1, alias = "치즈돈까스", menuName = "치즈돈까스"))
        every { menuV2Repository.findDistinctNames() } returns emptyList()
        val normalizer = MenuNameNormalizer(menuAliasV2Repository, menuV2Repository, embeddingModel)

        normalizer.init()
        val result = normalizer.normalize("매운돈까스")

        assertTrue(result.confidence < 0.90)
    }

    @Test
    fun `모델을 불러오지 못하면 DB 인덱스를 만들지 않고 입력값을 그대로 반환한다`() {
        val normalizer =
            MenuNameNormalizer(
                menuAliasV2Repository,
                menuV2Repository,
                FakeEmbeddingModel(emptyMap(), isReady = false),
            )

        normalizer.init()
        val result = normalizer.normalize("김치찌개")

        assertEquals("김치찌개", result.normalizedName)
        assertEquals(0.0, result.confidence)
        verify(exactly = 0) { menuAliasV2Repository.findAll() }
        verify(exactly = 0) { menuV2Repository.findDistinctNames() }
    }

    private class FakeEmbeddingModel(
        private val vectors: Map<String, FloatArray>,
        override val isReady: Boolean = true,
    ) : MenuNameEmbeddingModel {
        override fun embed(name: String): FloatArray? = vectors[name]
    }
}
