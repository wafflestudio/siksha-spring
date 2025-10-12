package siksha.wafflestudio.core.infrastructure.fcm

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service

@Service
class FcmService {
    fun sendNotification(
        targetToken: String,
        title: String,
        body: String,
    ) {
        val notification =
            Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build()

        val message =
            Message.builder()
                .setToken(targetToken)
                .setNotification(notification)
                .build()

        val response = FirebaseMessaging.getInstance().send(message)
    }
}
