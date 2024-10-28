package siksha.wafflestudio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@RestController
class SikshaApplication {

    @GetMapping("/ping")
    fun ping(): String {
        return "pong"
    }
}

fun main() {
    runApplication<SikshaApplication>()
}

