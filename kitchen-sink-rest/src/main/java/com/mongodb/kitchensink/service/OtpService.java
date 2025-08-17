package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.RedisValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();
    @Value("${otp.accountVerification.ttlSeconds}")
    private long accountVerificationTtl;

    @Value("${otp.forgotPassword.ttlSeconds}")
    private long forgotPasswordTtl;
    public OtpService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateOtp(String email, String type, Long ttlSeconds) {
        String redisKey = buildRedisKey(email, type);
        RedisValue<String> existingValue = (RedisValue<String>) redisTemplate.opsForValue().get(redisKey);
        if (existingValue != null && existingValue.isValid()) {
            return existingValue.getValue();
        }
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        long ttl = (ttlSeconds != null) ? ttlSeconds : getDefaultTtl(type);
        RedisValue<String> redisValue = new RedisValue<>(otp, ttl);
        redisTemplate.opsForValue().set(redisKey, redisValue, ttl, TimeUnit.SECONDS);

        return otp;
    }

    public boolean verifyOtp(String email, String type, String otp) {
        String redisKey = buildRedisKey(email, type);
        RedisValue<String> storedValue = (RedisValue<String>) redisTemplate.opsForValue().get(redisKey);
        boolean isValid = storedValue != null && storedValue.isValid() && storedValue.getValue().equals(otp);
        if (isValid) {
            redisTemplate.delete(redisKey);
        }
        return isValid;
    }

    public void clearOtp(String email, String type) {
        redisTemplate.delete(buildRedisKey(email, type));
    }

    private String buildRedisKey(String email, String type) {
        return String.format("OTP:%s:%s", type.toUpperCase(), email.toLowerCase());
    }
    private long getDefaultTtl(String type) {
        return switch (type) {
            case "ACCOUNT_VERIFICATION" -> accountVerificationTtl;
            case "FORGOT_PASSWORD" -> forgotPasswordTtl;
            default -> 300L;
        };
    }
}
