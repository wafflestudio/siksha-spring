package siksha.wafflestudio.core.infrastructure.slack

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class SlackNotifier(
    @Value("\${slack.token:}")
    private val slackToken: String?,
    @Value("\${slack.channel:}")
    private val slackChannel: String?,
) {
    private val slackUrl = "https://slack.com/api/chat.postMessage"
    private val restTemplate = RestTemplate()
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun sendSlackMessage(
        msg: String,
        platform: String? = null,
    ): Boolean {
        if (slackToken.isNullOrBlank() || slackChannel.isNullOrBlank()) {
            logger.error("Missing slack token or channel")
            return false
        }

        val text = if (platform.isNullOrBlank()) msg else "$msg\nplatform: $platform"
        val body =
            mapOf(
                "channel" to slackChannel,
                "text" to text,
            )
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(slackToken)
            }
        val request = HttpEntity(body, headers)

        try {
            val response =
                restTemplate.postForEntity(
                    slackUrl,
                    request,
                    String::class.java,
                )

            if (!response.statusCode.is2xxSuccessful) {
                logger.error("Failed to send slack message: $response")
                return false
            }
        } catch (e: Exception) {
            logger.error("Failed to send slack message: ${e.message}")
            return false
        }
        return true
    }
}
