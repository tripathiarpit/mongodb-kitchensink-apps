package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.RedisValue;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateOtp(String email, String type, long ttlSeconds) {
        String redisKey = buildRedisKey(email, type);

        // Check existing OTP
        RedisValue<String> existingValue = (RedisValue<String>) redisTemplate.opsForValue().get(redisKey);
        if (existingValue != null && existingValue.isValid()) {
            return existingValue.getValue();
        }

        // Create new OTP
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        RedisValue<String> redisValue = new RedisValue<>(otp, ttlSeconds);

        // Store with Redis TTL
        redisTemplate.opsForValue().set(redisKey, redisValue, ttlSeconds, TimeUnit.SECONDS);

        return otp;
    }

    public boolean verifyOtp(String email, String type, String otp) {
        String redisKey = buildRedisKey(email, type);
        RedisValue<String> storedValue = (RedisValue<String>) redisTemplate.opsForValue().get(redisKey);

        return storedValue != null && storedValue.isValid() && storedValue.getValue().equals(otp);
    }

    public void clearOtp(String email, String type) {
        redisTemplate.delete(buildRedisKey(email, type));
    }

    private String buildRedisKey(String email, String type) {
        return String.format("OTP:%s:%s", type.toUpperCase(), email.toLowerCase());
    }
}
