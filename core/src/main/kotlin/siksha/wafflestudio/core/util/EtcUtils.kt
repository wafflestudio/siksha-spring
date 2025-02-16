package siksha.wafflestudio.core.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.regex.Pattern


object EtcUtils {
    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val jsonEncoder = Json { encodeDefaults = true }

    fun getImageKeysFromUrlList(urls: List<String>): List<String> {
        val pattern = Pattern.compile("https://.*\\.s3\\.ap-northeast-2\\.amazonaws\\.com/(.*)")
        val keys = mutableListOf<String>()

        return urls.mapNotNull { url ->
            pattern.matcher(url).takeIf { it.find() } ?.group(1)
        }
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
