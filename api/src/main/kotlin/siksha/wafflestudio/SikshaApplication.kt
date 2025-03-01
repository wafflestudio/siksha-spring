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
    }
}

fun main() {
    runApplication<SikshaApplication>()
}
