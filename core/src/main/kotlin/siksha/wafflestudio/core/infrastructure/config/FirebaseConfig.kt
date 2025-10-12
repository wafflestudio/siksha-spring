package siksha.wafflestudio.core.infrastructure.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct
import java.io.FileInputStream

@Configuration
class FirebaseConfig {
    @PostConstruct
    fun init() {
        if (FirebaseApp.getApps().isEmpty()) {
            val serviceAccount = FileInputStream("/core/src/main/resources/firebase_service_key.json")

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            FirebaseApp.initializeApp(options)
        }
    }
}
