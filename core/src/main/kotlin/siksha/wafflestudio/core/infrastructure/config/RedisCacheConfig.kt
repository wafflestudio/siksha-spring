package siksha.wafflestudio.core.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class RedisCacheConfig {
    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val cacheConfigurations =
            mapOf(
                "boardCache" to createCacheConfiguration(Duration.ofDays(1)),
                "popularPostCache" to createCacheConfiguration(Duration.ofMinutes(10)),
                "bestPostCache" to createCacheConfiguration(Duration.ofMinutes(10)),
                "restaurantCache" to createCacheConfiguration(Duration.ofDays(1)),
            )

        return RedisCacheManager.builder(redisConnectionFactory)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }

    private fun createCacheConfiguration(ttl: Duration): RedisCacheConfiguration {
        return RedisCacheConfiguration.defaultCacheConfig(Thread.currentThread().contextClassLoader)
            .entryTtl(ttl)
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer()),
            )
    }

    private fun jsonSerializer(): RedisSerializer<Any> {
        val polymorphismResolver =
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Any::class.java)
                .build()

        return GenericJackson2JsonRedisSerializer(
            ObjectMapper().apply {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
                activateDefaultTyping(
                    polymorphismResolver,
                    ObjectMapper.DefaultTyping.EVERYTHING,
                )
            },
        )
    }
}
