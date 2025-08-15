package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.RedisValue;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class SessionService {

    private final RedisTemplate<String, Object> redisTemplate;

    public SessionService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRATION_SECONDS = 5 * 60; // 5 min
    private static final long SESSION_EXPIRATION_SECONDS = 30 * 60; // 30 min


    // Generate OTP for email
    public String generateOtp(String email) {
        String redisKey = "OTP:" + email;
        RedisValue<String> existing = (RedisValue<String>) redisTemplate.opsForValue().get(redisKey);

        if (existing != null && !existing.isExpired()) {
            return existing.getValue();
        }

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        RedisValue<String> value = new RedisValue<>(otp, OTP_EXPIRATION_SECONDS);
        redisTemplate.opsForValue().set(redisKey, value, Duration.ofSeconds(OTP_EXPIRATION_SECONDS));
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        String redisKey = "OTP:" + email;
        RedisValue<String> value = (RedisValue<String>) redisTemplate.opsForValue().get(redisKey);

        if (value == null || value.isExpired()) return false;
        return value.getValue().equals(otp);
    }

    public void storeSessionToken(String email, String token) {
        String redisKey = "SESSION:" + email;
        RedisValue<String> value = new RedisValue<>(token, SESSION_EXPIRATION_SECONDS);
        redisTemplate.opsForValue().set(redisKey, value, Duration.ofSeconds(SESSION_EXPIRATION_SECONDS));
    }

    public boolean validateSessionToken(String email, String token) {
        String redisKey = "SESSION:" + email;
        RedisValue<String> value = (RedisValue<String>) redisTemplate.opsForValue().get(redisKey);

        if (value == null || value.isExpired()) return false;

        value.refresh(SESSION_EXPIRATION_SECONDS);
        redisTemplate.opsForValue().set(redisKey, value, Duration.ofSeconds(SESSION_EXPIRATION_SECONDS));
        return value.getValue().equals(token);
    }

    public void invalidateSession(String email) {
        redisTemplate.delete("SESSION:" + email);
    }
    public boolean doesSessionExist(String email) {
        String key = "SESSION:" + email;
    RedisValue<String> sessionValue = (RedisValue<String>) redisTemplate.opsForValue().get(key);
        if (sessionValue == null) {
            return false;
        }
        if (sessionValue.isExpired()) {
            redisTemplate.delete(key);
            return false;
        }

        return true;
    }
    public String getTokenForExistingSession(String email) {
        String key = "SESSION:" + email;
        RedisValue<String> sessionValue = (RedisValue<String>) redisTemplate.opsForValue().get(key);
        if (sessionValue != null && !sessionValue.isExpired()) {
            return sessionValue.getValue();
        }
        if (sessionValue.isExpired()) {
            redisTemplate.delete(email);
            return null;
        }
        return null;
    }
}
