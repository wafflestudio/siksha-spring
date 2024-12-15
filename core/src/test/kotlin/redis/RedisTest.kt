package siksha.wafflestudio.core.redis

import junit.framework.TestCase.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import siksha.wafflestudio.core.infrastructure.cache.RedisService
import java.time.Duration
import kotlin.test.Test

@SpringBootTest
class RedisTest {
    @Autowired
    lateinit var redisService: RedisService

    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, Any>

    @Test
    fun testRedis() {
        val key = "worlds"
        val value = "make them believe"
        val duration = Duration.ofMinutes(1)

        redisService.cacheValue(key, value, duration)

        val cachedValue = redisService.getCachedValue<String>(key)
        println(cachedValue)

        assertEquals(value, cachedValue)
    }
}
