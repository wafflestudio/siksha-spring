package siksha.wafflestudio.core.util

import kotlinx.serialization.Serializable

@Serializable
data class Etc(
    val images: List<String> = emptyList(),
)
