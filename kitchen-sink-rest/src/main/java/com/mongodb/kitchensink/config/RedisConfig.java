package com.mongodb.kitchensink.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.kitchensink.constants.RedisValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

    @Configuration
    public class RedisConfig {



        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);

            // Use String serializer for keys
            template.setKeySerializer(new StringRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());

            // Create custom ObjectMapper for better JSON handling
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // For LocalDateTime support
            objectMapper.activateDefaultTyping(
                    objectMapper.getPolymorphicTypeValidator(),
                    ObjectMapper.DefaultTyping.NON_FINAL,
                    JsonTypeInfo.As.PROPERTY
            );

            // Use custom Jackson serializer
            GenericJackson2JsonRedisSerializer jsonSerializer =
                    new GenericJackson2JsonRedisSerializer(objectMapper);

            template.setValueSerializer(jsonSerializer);
            template.setHashValueSerializer(jsonSerializer);

            template.afterPropertiesSet();
            return template;
        }

    }