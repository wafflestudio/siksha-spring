package dto.version

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import siksha.wafflestudio.core.domain.v1.version.dto.VersionUpdateRequestDto

class VersionUpdateRequestDtoTest {
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `use snake case for version update request json`() {
        val request = objectMapper.readValue<VersionUpdateRequestDto>("""{"minimum_version":"3.5.1"}""")

        assertEquals("3.5.1", request.minimumVersion)

        val json = objectMapper.valueToTree<JsonNode>(request)
        assertEquals("3.5.1", json["minimum_version"].asText())
        assertFalse(json.has("minimumVersion"))
    }
}
