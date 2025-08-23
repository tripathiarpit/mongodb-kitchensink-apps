package com.mongodb.kitchensink.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import jakarta.annotation.PreDestroy;

/**
 * Class to Configure Serialization and Deserialization of Java Objects into the Format that REDIS can understand
 * <p>
 * Keys are serialized using StringRedisSerializer and values using GenericJackson2JsonRedisSerializer.
 * </p>
 * @author Arpit Tripathi
 * @version 1.0
 * @since 2025-08-17
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCleanup redisCleanup(RedisTemplate<String, Object> redisTemplate) {
        return new RedisCleanup(redisTemplate);
    }

    /**
     * Inner class for Redis cleanup logic.
     * <p>
     * Declared as a **static** nested class to prevent it from holding
     * a reference to the outer `RedisConfig` instance.
     * This makes it a standalone class that can be safely
     * instantiated by the Spring container.
     * </p>
     */
    public static class RedisCleanup {
        private final RedisTemplate<String, Object> redisTemplate;

        public RedisCleanup(RedisTemplate<String, Object> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        @PreDestroy
        public void clearRedisOnShutdown() {
            if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
                System.out.println("Clearing all Redis keys before shutdown...");
                redisTemplate.getConnectionFactory().getConnection().flushDb();
            }
        }
    }
}