package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.RedisValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class SessionService {
    @Value("${app.otp.length}")
    private int otpLength;

    @Value("${app.otp.expiration-seconds}")
    private long otpExpirationSeconds;

    @Value("${app.session.expiration-seconds}")
    private long sessionExpirationSeconds;
    private final RedisTemplate<String, Object> redisTemplate;

    public SessionService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    private final SecureRandom secureRandom = new SecureRandom();



    public String generateOtp(String email) {
        String redisKey = "OTP:" + email;
        RedisValue<String> existing = (RedisValue<String>) redisTemplate.opsForValue().get(redisKey);

        if (existing != null && !existing.isExpired()) {
            return existing.getValue();
        }

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        RedisValue<String> value = new RedisValue<>(otp, otpExpirationSeconds);
        redisTemplate.opsForValue().set(redisKey, value, Duration.ofSeconds(otpExpirationSeconds));
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
        RedisValue<String> value = new RedisValue<>(token, sessionExpirationSeconds);
        redisTemplate.opsForValue().set(redisKey, value, Duration.ofSeconds(sessionExpirationSeconds));
    }

    public void invalidateSessionToken(String email) {
        String redisKey = "SESSION:" + email;
        redisTemplate.delete(redisKey);
    }

    public boolean validateSessionToken(String email, String token) {
        String redisKey = "SESSION:" + email;
        RedisValue<String> value = (RedisValue<String>) redisTemplate.opsForValue().get(redisKey);
        if (value == null || value.isExpired()) {
            return false;
        }
        if (value.getValue().equals(token)) {
            value.refresh(sessionExpirationSeconds);
            redisTemplate.opsForValue().set(redisKey, value, Duration.ofSeconds(sessionExpirationSeconds));
            return true;
        }
        return false;
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
        if (sessionValue == null) {
            return null;
        }
        if (sessionValue.isExpired()) {
            redisTemplate.delete(email);
        }
        return sessionValue.getValue();
    }
}
