package siksha.wafflestudio.core.util

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.util.regex.Pattern

@Serializable
data class Etc(
    val images: List<String> = emptyList()
)

object EtcUtils {
    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val jsonEncoder = Json { encodeDefaults = true }

    fun getImageKeysFromUrlList(urls: List<String>): List<String> {
        val pattern = Pattern.compile("https://.*\\.s3\\.ap-northeast-2\\.amazonaws\\.com/(.*)")
        val keys = mutableListOf<String>()

        for (url in urls) {
            val matcher = pattern.matcher(url)
            if (matcher.find()) {
                keys.add(matcher.group(1) ?: "")
            }
        }

        return keys
    }

    fun parseImageUrlsFromEtc(etcJson: String?): List<String> {
        return etcJson?.let {
            runCatching {
                jsonParser.decodeFromString<Etc>(it).images
            }.getOrDefault(emptyList())
        } ?: emptyList()
    }

    fun convertImageUrlsToEtcJson(imageUrls: List<String>): String? {
        val etc = Etc(images = imageUrls)
        return jsonEncoder.encodeToString<Etc>(etc)
    }
}

