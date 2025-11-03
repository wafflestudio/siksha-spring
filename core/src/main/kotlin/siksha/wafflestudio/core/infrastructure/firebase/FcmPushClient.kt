package siksha.wafflestudio.core.infrastructure.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.ErrorCode
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.Aps
import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import siksha.wafflestudio.core.domain.user.data.UserDevice

/*
    사용 예시

    @Service
    class ExampleService(
        private val fcmPushClient: FcmPushClient,
    ) {
        fun exampleMethod() {
            val pushMessage = PushMessage(
                title = "example title",
                body = "example body",
            )

            val userDevices: List<UserDevice> = // get user devices

            fcmPushClient.sendPushMessages(pushMessage, userDevices)
        }
      }
    )

 */

@Component
@ConditionalOnProperty(name = ["siksha.firebase.service-account"]) // 테스트 환경에서는 비활성화 (임의의 credential을 넣으면 인스턴스화 실패하기 때문)
class FcmPushClient(
    @Value("\${siksha.firebase.service-account}") private val serviceAccountJson: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    init {
        val options =
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountJson.byteInputStream()))
                .setDatabaseUrl("https://siksha-dev.firebaseio.com/")
                .build()

        FirebaseApp.initializeApp(options)
    }

    fun sendPushMessages(
        pushMessage: PushMessage,
        userDevices: List<UserDevice>,
    ) {
        log.info("[FCM] sending push to ${userDevices.size} devices. title: ${pushMessage.title} body:${pushMessage.body}")

        userDevices.chunked(FCM_BATCH_COUNT_LIMIT).forEach { deviceChunk ->
            val messages =
                deviceChunk.map {
                    pushMessage.toFcmMessage(it.fcmToken)
                }

            val response = FirebaseMessaging.getInstance().sendEach(messages)

            log.info("[FCM] ${response.successCount} messages sent successfully")
            logExceptions(response)
        }
    }

    private fun PushMessage.toFcmMessage(fcmToken: String): Message {
        val notification =
            Notification.builder()
                .setTitle(title)
                .setBody(body)
                .apply { image?.let { setImage(it) } }
                .build()

        val androidConfig =
            collapseKey?.let {
                AndroidConfig.builder().setCollapseKey(it).setPriority(AndroidConfig.Priority.HIGH).setTtl(collapseTtl)
                    .build()
            }

        val apnsConfig =
            collapseKey?.let {
                val aps = Aps.builder().setThreadId(it).build()
                ApnsConfig.builder().putHeader("apns-priority", "5").setAps(aps)
                    .build()
            }

        val fcmMessage =
            Message.builder()
                .setToken(fcmToken)
                .setNotification(notification)
                .apply {
                    androidConfig?.let { setAndroidConfig(it) }
                    apnsConfig?.let { setApnsConfig(it) }
                }
                .build()

        return fcmMessage
    }

    private fun logExceptions(batchResponse: BatchResponse) {
        if (batchResponse.failureCount > 0) {
            batchResponse.responses.forEach { sendResponse ->
                sendResponse.exception?.let {
                    if (it.errorCode != ErrorCode.NOT_FOUND) {
                        log.error("[FCM ERROR]", it)
                    }
                }
            }
        }
    }

    companion object {
        private const val FCM_BATCH_COUNT_LIMIT = 499
    }
}

data class PushMessage(
    val title: String,
    val body: String,
    val image: String? = null,
    val collapseKey: String? = null,
    val collapseTtl: Long = 86400,
)
