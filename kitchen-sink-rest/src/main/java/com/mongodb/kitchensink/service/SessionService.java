package com.mongodb.kitchensink.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.kitchensink.config.AppSessionConfig;
import com.mongodb.kitchensink.config.OtpConfig;
import com.mongodb.kitchensink.constants.RedisValue; // Uses your provided RedisValue
import com.mongodb.kitchensink.dto.ApplicationSettingsPayload;
import com.mongodb.kitchensink.util.TimeCalculationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration; // For RedisTemplate TTLs
import java.time.LocalDateTime; // For RedisValue's timestamp field
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit; // For RedisTemplate TTLs

@Service
public class SessionService {

    private final ResourceConfigService resourceConfigService;
    @Value("${app.otp.length}")
    private int otpLength;

    // Default expiration seconds from application properties.
    @Value("${app.otp.expiration-seconds}")
    private long defaultOtpExpirationSeconds;

    @Value("${app.session.expiration-seconds}")
    private long defaultSessionExpirationSeconds;

    private final RedisTemplate<String, Object> redisTemplate;
    private final TimeCalculationUtil timeCalculationUtil;

    private final SecureRandom secureRandom = new SecureRandom();

    public SessionService(RedisTemplate<String, Object> redisTemplate, TimeCalculationUtil timeCalculationUtil, ResourceConfigService resourceConfigService) {
        this.redisTemplate = redisTemplate;
        this.timeCalculationUtil = timeCalculationUtil;
        this.resourceConfigService = resourceConfigService;
    }

    /**
     * Helper method to build Redis keys with consistent prefixes.
     * @param identifier The user's email or other unique ID.
     * @param type The type of Redis entry (e.g., "SESSION", "FORGOT_OTP", "REG_OTP").
     * @return The formatted Redis key string.
     */
    private String buildRedisKey(String identifier, String type) {
        return type.toUpperCase() + ":" + identifier;
    }

    /**
     * Helper method to get the default TTL based on the entry type.
     * @param type The type of Redis entry.
     * @return The default TTL in seconds for that type.
     */
    private long getDefaultTtl(String type) {
        switch (type.toUpperCase()) {
            case "SESSION":
                return defaultSessionExpirationSeconds;
            case "FORGOT_OTP":
            case "REG_OTP": // Both OTP types share the same default expiration unless specified otherwise
                return defaultOtpExpirationSeconds;
            default:
                // Log a warning or throw an exception if an unknown type is passed
                System.err.println("Warning: Unknown Redis key type requested for default TTL: " + type);
                return 0; // Return 0 or a safe default
        }
    }

    /**
     * Generic method to generate and store an OTP in Redis.
     * It checks for an existing active OTP before generating a new one.
     * @param email The user's email.
     * @param type The type of OTP (e.g., "FORGOT_OTP", "REG_OTP").
     * @param ttlSeconds Optional, specific TTL for this OTP instance. If null, uses default for the type.
     * @return The generated OTP string.
     */
    public String generateOtp(String email, String type, Long ttlSeconds) {
        String redisKey = buildRedisKey(email, type);
        RedisValue<String> existingValue = (RedisValue<String>) redisTemplate.opsForValue().get(redisKey);

        // Use the isValid() method from the user's RedisValue to check if active
        if (existingValue != null && existingValue.isValid()) {
            System.out.println("Returning existing active OTP for " + type + ":" + email);
            return existingValue.getValue();
        }

        String otp = String.format("%0" + otpLength + "d", secureRandom.nextInt((int) Math.pow(10, otpLength)));
        long actualTtl = (ttlSeconds != null) ? ttlSeconds : getDefaultTtl(type);

        // Create RedisValue using the user's constructor (which sets timestamp = now and ttl)
        RedisValue<String> redisValue = new RedisValue<>(otp, actualTtl);
        // Set in Redis using the RedisValue object and its internal TTL from the constructor
        redisTemplate.opsForValue().set(redisKey, redisValue, actualTtl, TimeUnit.SECONDS);

        System.out.println("Generated new OTP for " + type + ":" + email + " with TTL " + actualTtl + "s");
        return otp;
    }

    // Updated generateForgotPasswordOtp to use the generic method
    public String generateForgotPasswordOtp(String email) {
        // When generating a *new* OTP, it should use the currently configured default expiry.
        return generateOtp(email, "FORGOT_OTP", defaultOtpExpirationSeconds);
    }

    // Updated generateRegistrationOtp to use the generic method
    public String generateRegistrationOtp(String email) {
        return generateOtp(email, "REG_OTP", defaultOtpExpirationSeconds);
    }

    /**
     * Validates a generic OTP against the stored value in Redis.
     * @param email The user's email.
     * @param type The type of OTP.
     * @param otp The OTP string to validate.
     * @return true if the OTP is valid and active, false otherwise.
     */
    public boolean validateOtp(String email, String type, String otp) {
        String redisKey = buildRedisKey(email, type);
        RedisValue<String> value = (RedisValue<String>) redisTemplate.opsForValue().get(redisKey);

        // Use isValid() from the user's RedisValue
        if (value == null || !value.isValid()) {
            System.out.println("OTP " + type + ":" + email + " not found or expired.");
            return false;
        }

        boolean isValid = value.getValue().equals(otp);
        if (isValid) {
            System.out.println("OTP " + type + ":" + email + " validated successfully.");
            // Optionally delete OTP after successful validation if it's a one-time use
            redisTemplate.delete(redisKey);
        } else {
            System.out.println("OTP " + type + ":" + email + " validation failed (mismatch).");
        }
        return isValid;
    }

    public boolean validateForgotPasswordOtp(String email, String otp) {
        return validateOtp(email, "FORGOT_OTP", otp);
    }

    public boolean validateRegistrationOtp(String email, String otp) {
        return validateOtp(email, "REG_OTP", otp);
    }

    /**
     * Stores a user's session token in Redis.
     * @param email The user's email.
     * @param token The session token string.
     */
    public void storeSessionToken(String email, String token) {
        String redisKey = buildRedisKey(email, "SESSION");
        long ttl = defaultSessionExpirationSeconds; // Initial TTL for new sessions
        RedisValue<String> value = new RedisValue<>(token, ttl); // Use user's constructor
        redisTemplate.opsForValue().set(redisKey, value, ttl, TimeUnit.SECONDS);
        System.out.println("Session token stored for " + email + " with TTL " + ttl + "s.");
    }

    /**
     * Invalidates (deletes) a specific session token for a user.
     * @param email The user's email.
     */
    public void invalidateSessionToken(String email) {
        String redisKey = buildRedisKey(email, "SESSION");
        redisTemplate.delete(redisKey);
        System.out.println("Session token invalidated for " + email);
    }

    /**
     * Validates a session token. If valid and not expired, refreshes its expiry.
     * @param email The user's email.
     * @param token The session token to validate.
     * @return true if the session token is valid and refreshed, false otherwise.
     */
    public boolean validateSessionToken(String email, String token) {
        String redisKey = buildRedisKey(email, "SESSION");
        RedisValue<String> value = (RedisValue<String>) redisTemplate.opsForValue().get(redisKey);

        if (value == null || !value.isValid()) { // Use isValid()
            if (value != null) { // If it was an expired value still in Redis, clean it up
                redisTemplate.delete(redisKey);
            }
            System.out.println("Session for " + email + " not found or expired.");
            return false;
        }

        if (!value.getValue().equals(token)) {
            System.out.println("Session token mismatch for " + email);
            return false;
        }
        Optional<AppSessionConfig> sessionConfig = resourceConfigService.getConfig("appSessionConfig", AppSessionConfig.class);
        long currentSessionTtl = sessionConfig.isPresent()? sessionConfig.get().getExpirationSeconds():defaultSessionExpirationSeconds;
        value.refresh(currentSessionTtl); // Updates internal timestamp to now, and ttl to currentSessionTtl
        redisTemplate.opsForValue().set(redisKey, value, currentSessionTtl, TimeUnit.SECONDS); // Reset Redis TTL

        System.out.println("Session for " + email + " validated and refreshed with TTL " + currentSessionTtl + "s.");
        return true;
    }

    /**
     * Invalidates (deletes) a user's session from Redis.
     * @param email The user's email.
     */
    public void invalidateSession(String email) {
        redisTemplate.delete(buildRedisKey(email, "SESSION"));
        System.out.println("Session invalidated for " + email);
    }

    /**
     * Checks if a session exists and is not expired for a given email.
     * @param email The user's email.
     * @return true if an active session exists, false otherwise.
     */
    public boolean doesSessionExist(String email) {
        String key = buildRedisKey(email, "SESSION");
        RedisValue<String> sessionValue = (RedisValue<String>) redisTemplate.opsForValue().get(key);
        if (sessionValue == null) {
            return false;
        }
        // Use isValid() from the user's RedisValue
        if (!sessionValue.isValid()) {
            redisTemplate.delete(key); // Clean up expired session if found
            return false;
        }
        return true;
    }

    /**
     * Retrieves the session token for an existing active session.
     * @param email The user's email.
     * @return The session token if active, null otherwise.
     */
    public String getTokenForExistingSession(String email) {
        String key = buildRedisKey(email, "SESSION");
        RedisValue<String> sessionValue = (RedisValue<String>) redisTemplate.opsForValue().get(key);
        // Use isValid() from the user's RedisValue
        if (sessionValue != null && sessionValue.isValid()) {
            return sessionValue.getValue();
        }
        if (sessionValue != null && !sessionValue.isValid()) { // If it was an expired value still in Redis
            redisTemplate.delete(key); // Clean it up
        }
        return null;
    }

    /**
     * Applies new application settings to existing Redis cache entries (sessions and OTPs).
     * This method iterates through relevant Redis keys and updates their Time-To-Live (TTL)
     * and the internal timestamp and ttl fields of the stored RedisValue objects.
     *
     * Given the RedisValue structure (timestamp is updated on refresh, TTL is from timestamp),
     * applying new global settings means giving existing active items a new lease on life
     * based on the *new total duration* starting from the moment the settings are applied.
     * Expired entries will also be given a fresh new life if they are encountered.
     *
     * @param payload The {@link ApplicationSettingsPayload} containing the new expiry settings (in seconds).
     */
    public void saveApplicationSettingsAndApply(ApplicationSettingsPayload payload) throws JsonProcessingException {
        System.out.println("Applying new application settings to Redis: " + payload);

        long newSessionExpirySeconds = payload.getSessionExpirySeconds();
        long newForgotOtpExpirySeconds = payload.getForgotPasswordOtpExpirySeconds();
        long newUserRegistrationOtpExpirySeconds = payload.getUserRegistrationOtpExpirySeconds();
        if(newSessionExpirySeconds>0)
        resourceConfigService.saveConfig("appSessionConfig", new AppSessionConfig(newSessionExpirySeconds));
        if(newForgotOtpExpirySeconds >0 &&  newUserRegistrationOtpExpirySeconds >0) {
            resourceConfigService.saveConfig("otpConfig", new OtpConfig(newUserRegistrationOtpExpirySeconds,newForgotOtpExpirySeconds,6L, 3600L));
        }
        Set<String> sessionKeys = redisTemplate.keys("SESSION:*");
        Set<String> forgotOtpKeys = redisTemplate.keys("FORGOT_OTP:*");
        Set<String> regOtpKeys = redisTemplate.keys("REG_OTP:*");

        Set<String> allRelevantKeys = new HashSet<>();
        if (sessionKeys != null) allRelevantKeys.addAll(sessionKeys);
        if (forgotOtpKeys != null) allRelevantKeys.addAll(forgotOtpKeys);
        if (regOtpKeys != null) allRelevantKeys.addAll(regOtpKeys);

        if (allRelevantKeys.isEmpty()) {
            System.out.println("No relevant Redis keys found to update.");
            return;
        }

        for (String key : allRelevantKeys) {
            try {
                RedisValue<?> existingRedisValue = (RedisValue<?>) redisTemplate.opsForValue().get(key);

                if (existingRedisValue == null) {
                    continue;
                }

                long newConfigTotalSeconds;
                if (key.startsWith("SESSION:")) {
                    newConfigTotalSeconds = newSessionExpirySeconds;
                } else if (key.startsWith("FORGOT_OTP:")) {
                    newConfigTotalSeconds = newForgotOtpExpirySeconds;
                } else if (key.startsWith("REG_OTP:")) {
                    newConfigTotalSeconds = newUserRegistrationOtpExpirySeconds;
                } else {
                    System.out.println("Skipping unrecognized Redis key pattern: " + key);
                    continue;
                }
                existingRedisValue.setTtl(newConfigTotalSeconds);
                existingRedisValue.setTimestamp(LocalDateTime.now());
                existingRedisValue.setExpired(false);


                long redisTTL = timeCalculationUtil.calculateNewRedisTTL(existingRedisValue, newConfigTotalSeconds);

                // Save the modified RedisValue object back to Redis and set its TTL
                redisTemplate.opsForValue().set(key, existingRedisValue, redisTTL, TimeUnit.SECONDS);

                System.out.println("Updated key: " + key + " with new internal TTL: " + newConfigTotalSeconds +
                        "s and reset timestamp. Redis TTL set to: " + redisTTL + "s.");
                    resourceConfigService.saveConfig(key, newConfigTotalSeconds);
            } catch (Exception e) {
                System.err.println("Error processing Redis key " + key + " during settings update: " + e.getMessage());
                // Log the error and continue to process other keys
            }
        }
        System.out.println("Finished applying new application settings to Redis.");
    }
}
