package siksha.wafflestudio.core.infrastructure.cache

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, Any>,
    @Qualifier("redisConnectionFactory") private val redisConnectionFactory: RedisConnectionFactory,
) {
    fun <T> cacheValue(
        key: String, value: T, expire: Duration
    ) {
        try {
            redisTemplate.opsForValue().set(key, value as Any, expire)
        } catch (e: Exception) {
            println("Error setting cache value: ${e.message}")
        }
    }

    fun <T> getCachedValue(
        key: String
    ): T? {
        return try {
            redisTemplate.opsForValue().get(key) as? T
        } catch (e: Exception) {
            println("Error retrieving cached value: ${e.message}")
            null
        }
    }

    fun deleteByKey(
        key: String
    ): Boolean {
        return try {
            redisTemplate.delete(key)
        } catch (e: Exception) {
            println("Error deleting cached by key ${e.message}")
            false
        }
    }

    fun clearAllCache() {
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll();
        println("All cache cleared")
    }
}
