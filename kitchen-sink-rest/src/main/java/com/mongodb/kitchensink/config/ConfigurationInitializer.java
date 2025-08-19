package com.mongodb.kitchensink.config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.kitchensink.service.ResourceConfigService; // Updated import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.Optional;


@Component
public class ConfigurationInitializer implements CommandLineRunner {
    private final Environment env;
    @Value("${jwt.secret:MySuperSecretKeyForJwtWhichIsAtLeast256BitsLong!}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:3600000}")
    private long jwtExpirationMs;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.timeout:60000}")
    private long redisTimeout;

    @Value("${mail.host:smtp.gmail.com}")
    private String mailHost;

    @Value("${mail.port:587}")
    private int mailPort;

    @Value("${mail.username:tripathisrule12@gmail.com}")
    private String mailUsername;

    @Value("${mail.password:uuog tkww jpjm nxpj}")
    private String mailPassword;

    @Value("${otp.accountVerification.ttlSeconds:7200}")
    private int otpAccountVerificationTtlSeconds;

    @Value("${otp.forgotPassword.ttlSeconds:300}")
    private int otpForgotPasswordTtlSeconds;

    @Value("${app.otp.length:6}")
    private int appOtpLength;

    @Value("${app.otp.expiration-seconds:300}")
    private int appOtpExpirationSeconds;

    @Value("${app.session.expiration-seconds:3600}")
    private int appSessionExpirationSeconds;
    private final ResourceConfigService resourceConfigService;

    @Autowired
    public ConfigurationInitializer(ResourceConfigService resourceConfigService, Environment env) {
        this.resourceConfigService = resourceConfigService;
        this.env = env;
    }
    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- Initializing default application configurations ---");
        initializeConfig("jwtConfig", new JwtConfig(jwtSecret, jwtExpirationMs));
        initializeConfig("redisConfig", new RedisConfig(resourceConfigService,env,redisHost, redisPort, redisPassword, redisTimeout));
        initializeConfig("mailConfig", new MailConfig(mailHost, mailPort, mailUsername, mailPassword));
        initializeConfig("otpConfig", new OtpConfig((long)otpAccountVerificationTtlSeconds, (long)otpForgotPasswordTtlSeconds, (long)appOtpLength, (long)appOtpExpirationSeconds));
        initializeConfig("appSessionConfig", new AppSessionConfig(appSessionExpirationSeconds));
        System.out.println("--- Default application configurations initialized ---");
    }

    /**
     * Helper method to initialize a configuration.
     * It checks if a config with the given key exists in the database.
     * If not, it saves the provided defaultConfig object.
     *
     * @param configKey The unique key for the configuration.
     * @param defaultConfig The default configuration object to save if not found.
     * @param <T> The type of the configuration object.
     */
    private <T> void initializeConfig(String configKey, T defaultConfig) {
        try {
            Optional<T> existingConfig = resourceConfigService.getConfig(configKey, (Class<T>) defaultConfig.getClass());
            if (existingConfig.isEmpty()) {
                resourceConfigService.saveConfig(configKey, defaultConfig);
                System.out.println("Saved default config for: " + configKey);
            } else {
                System.out.println("Config already exists for: " + configKey + ". Using persisted value.");
            }
        } catch (JsonProcessingException e) {
            System.err.println("Error initializing config for " + configKey + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error initializing config for " + configKey + ": " + e.getMessage());
        }
    }
}
