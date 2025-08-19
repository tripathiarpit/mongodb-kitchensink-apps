// src/main/java/com/mongodb/kitchensink/config/RedisConfig.java
package com.mongodb.kitchensink.config;

import com.mongodb.kitchensink.service.ResourceConfigService;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment; // Import Spring's Environment
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;

@Configuration
public class RedisConfig {

    private  ResourceConfigService resourceConfigService;
    private  Environment env; // Inject Environment

    private String host;
    private int port;

    public Environment getEnv() {
        return env;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    private String password;
    private long timeout;

    public RedisConfig(String host, int port, String password, long timeout) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.timeout = timeout;
    }

    // This field holds the RedisTemplate bean instance for use in @PreDestroy.
    private RedisTemplate<String, Object> redisTemplateInstance;


    @Autowired
    public RedisConfig(
            ResourceConfigService resourceConfigService,
            Environment env, // Inject Environment here
            @Value("${spring.data.redis.host:localhost}") String initialHost,
            @Value("${spring.data.redis.port:6379}") int initialPort,
            @Value("${spring.data.redis.password:}") String initialPassword,
            @Value("${spring.data.redis.timeout:60000}") long initialTimeout) {
        this.resourceConfigService = resourceConfigService;
        this.env = env; // Store the injected Environment

        // Initialize fields with values from properties/environment.
        // These serve as fallbacks before DB values are loaded.
        this.host = initialHost;
        this.port = initialPort;
        this.password = initialPassword;
        this.timeout = initialTimeout;
    }

    /**
     * This method runs after the constructor and initial @Value injection.
     * It attempts to load Redis configuration from the database. If found,
     * these database values override the initial properties/environment values.
     * This ensures database persistence takes precedence.
     */
    @PostConstruct
    public void loadRedisConfigurationFromDb() {
        System.out.println("Attempting to load Redis configuration from database...");
        java.util.Optional<com.mongodb.kitchensink.config.RedisConfig> dbConfig =
                resourceConfigService.getConfig("redisConfig", com.mongodb.kitchensink.config.RedisConfig.class);

        if (dbConfig.isPresent()) {
            com.mongodb.kitchensink.config.RedisConfig loadedConfig = dbConfig.get();
            // Override instance fields with values from the database
            this.host = loadedConfig.getHost();
            this.port = loadedConfig.getPort();
            this.password = loadedConfig.getPassword();
            this.timeout = loadedConfig.getTimeout();
            System.out.println("Redis configuration loaded from DB: Host=" + host + ", Port=" + port + ", Timeout=" + timeout + "ms");
        } else {
            System.out.println("Redis configuration not found in DB. Using properties/environment values (already set in constructor).");
        }
    }

    /**
     * Defines the `RedisConnectionFactory` bean.
     * It directly retrieves host, port, password, and timeout from the `Environment`
     * to ensure the most up-to-date and available values are used at the time of bean creation.
     * This is the most robust way to ensure properties are present.
     *
     * @return A configured `RedisConnectionFactory` (using the Lettuce implementation).
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Retrieve values directly from Environment, with default fallbacks
        String currentHost = env.getProperty("spring.data.redis.host", "localhost");
        int currentPort = env.getProperty("spring.data.redis.port", Integer.class, 6379);
        String currentPassword = env.getProperty("spring.data.redis.password", "");
        long currentTimeout = env.getProperty("spring.data.redis.timeout", Long.class, 60000L);


        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(currentHost);
        config.setPort(currentPort);

        if (currentPassword != null && !currentPassword.isEmpty()) {
            config.setPassword(currentPassword);
        }

        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(config);

        if (currentTimeout > 0) {
            lettuceConnectionFactory.setShutdownTimeout(currentTimeout);
        }

        lettuceConnectionFactory.afterPropertiesSet();
        return lettuceConnectionFactory;
    }

    /**
     * Defines the `RedisTemplate` bean, providing a high-level abstraction for Redis operations.
     *
     * @param connectionFactory The `RedisConnectionFactory` bean, automatically injected by Spring.
     * @return A configured `RedisTemplate` instance.
     */
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
        // Assign the created RedisTemplate instance to the class field for @PreDestroy access
        this.redisTemplateInstance = template;
        return template;
    }

    /**
     * This method is called just before the application context is closed (on shutdown).
     * It flushes (clears) the entire Redis database using the stored `redisTemplateInstance`.
     * IMPORTANT: Flushing the database can lead to data loss. Use with extreme caution.
     */
    @PreDestroy
    public void clearRedisOnShutdown() {
        if (this.redisTemplateInstance != null && this.redisTemplateInstance.getConnectionFactory() != null) {
            System.out.println("Clearing all Redis keys before shutdown...");
            try {
                this.redisTemplateInstance.getConnectionFactory().getConnection().flushDb();
                System.out.println("Redis database flushed successfully.");
            } catch (Exception e) {
                System.err.println("Error flushing Redis database on shutdown: " + e.getMessage());
            }
        }
    }
}
