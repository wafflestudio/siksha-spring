package siksha.wafflestudio.core.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
            pattern.matcher(url).takeIf { it.find() }?.group(1)
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

    // restaurant의 etc 필드가 JSON 형태로 저장되어 있을 때, 이를 JsonNode로 변환
    fun convertRestEtc(etc: String?): JsonNode {
        return if (etc.isNullOrBlank()) {
            jacksonObjectMapper().createObjectNode()
        } else {
            jacksonObjectMapper().readTree(etc)
        }
    }

    // menu의 etc 필드가 JSON 형태로 저장되어 있을 때, 이를 List<String>으로 변환
    fun convertMenuEtc(etc: String?): List<String> {
        if (etc.isNullOrBlank() || etc.trim() == "[]") {
            return emptyList()
        }
        return try {
            jacksonObjectMapper().readValue(etc, object : TypeReference<List<String>>() {})
        } catch (e: Exception) {
            emptyList()
        }
    }
}
