package siksha.wafflestudio.core.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class OffsetDateTimeDeserializer : JsonDeserializer<OffsetDateTime>() {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

    override fun deserialize(parser: JsonParser, context: DeserializationContext): OffsetDateTime {
        return OffsetDateTime.parse(parser.text, formatter)
    }
}

