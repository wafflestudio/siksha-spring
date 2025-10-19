package siksha.wafflestudio.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import siksha.wafflestudio.core.domain.basic.dto.VocRequest
import siksha.wafflestudio.core.infrastructure.slack.SlackNotifier

@RestController
@Tag(name = "Basic")
class BasicController(
    private val slackNotifier: SlackNotifier
) {
    @PostMapping("/voc")
    @ResponseStatus(HttpStatus.CREATED)
    fun postVoc(
        @RequestBody request: VocRequest,
    ) {
        slackNotifier.sendSlackMessage(request.voc, request.platform)
    }
}
