package siksha.wafflestudio.core.domain.main.restaurant.data

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class CustomV2Document(
    val items: MutableMap<String, CustomV2Item> = linkedMapOf(),
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CustomV2Item(
    val order: Int? = null,
    val visible: Boolean? = null,
) {
    fun isComplete(): Boolean = order != null && visible != null
}

object CustomV2Json {
    private val objectMapper =
        jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun parse(customs: String?): CustomV2Document =
        if (customs.isNullOrBlank()) {
            CustomV2Document()
        } else {
            runCatching { objectMapper.readValue(customs, CustomV2Document::class.java) }
                .getOrDefault(CustomV2Document())
        }

    fun stringify(document: CustomV2Document): String = objectMapper.writeValueAsString(document)
}

fun CustomV2Document.itemOf(id: Int): CustomV2Item? = items[id.toString()]

fun CustomV2Document.setCustom(
    id: Int,
    order: Int,
    visible: Boolean,
) {
    items[id.toString()] = CustomV2Item(order = order, visible = visible)
}
