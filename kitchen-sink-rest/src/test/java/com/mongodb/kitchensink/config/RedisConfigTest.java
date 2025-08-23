package com.mongodb.kitchensink.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RedisConfig.class)
@DisplayName("RedisConfig Test")
class RedisConfigTest {

    @MockBean
    private RedisConnectionFactory mockConnectionFactory;

    // Mock RedisConnection to verify the flushDb() call.
    @MockBean
    private RedisConnection mockConnection;

    // Autowire the beans created by RedisConfig.
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisConfig.RedisCleanup redisCleanup;



    @Test
    @DisplayName("RedisTemplate bean should be correctly configured")
    void redisTemplate_shouldBeCorrectlyConfigured() {
        assertThat(redisTemplate).isNotNull();
        assertThat(redisTemplate.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(redisTemplate.getHashKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(redisTemplate.getValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);
        assertThat(redisTemplate.getHashValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);
    }


    @Test
    @DisplayName("RedisCleanup should flush the database on shutdown")
    void redisCleanup_shouldFlushDbOnShutdown() {
        // Given a mock RedisConnectionFactory that returns a mock connection
        when(mockConnectionFactory.getConnection()).thenReturn(mockConnection);

        // When the clearRedisOnShutdown() method is called
        redisCleanup.clearRedisOnShutdown();

        // Then verify the interaction with the mocked objects
        verify(mockConnectionFactory, times(1)).getConnection();
        verify(mockConnection, times(1)).flushDb();
    }
}