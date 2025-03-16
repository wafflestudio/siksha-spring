package siksha.wafflestudio

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.TimeZone

@SpringBootApplication
class SikshaApplication {
    @PostConstruct
    fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
        // DB에는 UTC 기준 시간을 사용
        // JVM은 Asia/Seoul 기준으로 동작하여, response는 KST 기준으로 timezone 정보 (+09:00)을 포함하여 내려감
    }
}

fun main() {
    runApplication<SikshaApplication>()
}
