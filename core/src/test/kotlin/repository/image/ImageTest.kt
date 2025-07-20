package siksha.wafflestudio.core.repository.image

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import siksha.wafflestudio.core.domain.image.data.Image
import siksha.wafflestudio.core.domain.image.data.ImageCategory
import siksha.wafflestudio.core.domain.image.repository.ImageRepository
import kotlin.test.assertNotNull

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ImageTest {
    @Autowired
    lateinit var repository: ImageRepository

    @Test
    fun `save board`() {
        // when
        val image = Image(
            category = ImageCategory.POST,
            key = "test",
            userId = 1
        )
        val savedImage = repository.save(image)

        // then
        assertNotNull(savedImage)
        assertEquals(image.key, savedImage.key)
    }
}
