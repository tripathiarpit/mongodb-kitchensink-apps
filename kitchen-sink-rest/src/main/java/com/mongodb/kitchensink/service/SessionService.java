package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.RedisValue;
import com.mongodb.kitchensink.exception.JwtExpiredException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static com.mongodb.kitchensink.constants.AppContants.ACTIVE_ACCESS_TOKEN;
import static com.mongodb.kitchensink.constants.AppContants.REFRESH_TOKEN;
import static com.mongodb.kitchensink.constants.ErrorMessageConstants.TOKEN_EXPIRED;

@Service
public class SessionService {
    @Value("${app.otp.length}")
    private int otpLength;

    @Value("${app.otp.expiration-seconds}")
    private long otpExpirationSeconds;
    @Value("${jwt.refresh-expiration-seconds}")
    private long refreshTokenExpirationSeconds;


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
    public void storeRefreshToken(String email, String refreshToken, long expirationSeconds) {
        RedisValue<String> sessionValue = new RedisValue<>(refreshToken, expirationSeconds);
        String key = REFRESH_TOKEN+":" + email;
        redisTemplate.opsForValue().set(key, sessionValue, Duration.ofSeconds(expirationSeconds));
    }
    public void storeAccessToken(String email, String accessToken, long expirationSeconds) {
        RedisValue<String> sessionValue = new RedisValue<>(accessToken, expirationSeconds);
        String key = ACTIVE_ACCESS_TOKEN+":" + email;
        redisTemplate.opsForValue().set(key, sessionValue, Duration.ofSeconds(expirationSeconds));
    }
    public boolean validateAndConsumeRefreshToken(String email, String refreshToken) {

        String redisKey = REFRESH_TOKEN+":" + email;
        RedisValue<String> storedValue = (RedisValue<String>) redisTemplate.opsForValue().get(redisKey);
        if (storedValue == null || storedValue.isExpired()) {
            return false;
        }
        if (storedValue.getValue().equals(refreshToken)) {
            redisTemplate.delete(redisKey);
            return true;
        }

        return false;
    }
    public void invalidateSession(String email) {
        String keyRefreshToken =REFRESH_TOKEN+":" + email;
        String keyAccessToken = ACTIVE_ACCESS_TOKEN+":" + email;
        if(this.doesSessionExist(email)) {
            redisTemplate.delete(List.of(keyRefreshToken,keyAccessToken));
        }
    }
    public boolean validateSessionToken(String email, String accessToken) {
        if (accessToken == null) {
            return false;
        }
        String key = ACTIVE_ACCESS_TOKEN + ":" + email;
        Object storedToken = redisTemplate.opsForValue().get(key);
        return accessToken.equals(storedToken);
    }
    public void validateAndRefreshExistingSessionExpiry(String email, long accessTokenExpirationSeconds) {
        if(doesSessionExist(email)){
            String key = ACTIVE_ACCESS_TOKEN+":" + email;
            RedisValue<String> sessionValue = (RedisValue<String>) redisTemplate.opsForValue().get(key);
            if (sessionValue != null) {
                storeAccessToken(email,sessionValue.getValue(), accessTokenExpirationSeconds);
            } else {
                redisTemplate.delete(key);
                throw new JwtExpiredException(ErrorCodes.SESSION_EXPIRED, TOKEN_EXPIRED);
            }
        } else {
            throw new JwtExpiredException(ErrorCodes.SESSION_EXPIRED, TOKEN_EXPIRED);
        }
    }
    public boolean  doesSessionExist(String email) {
        try {
            String key = ACTIVE_ACCESS_TOKEN+":" + email;
            RedisValue<String> sessionValue = (RedisValue<String>) redisTemplate.opsForValue().get(key);
            if (sessionValue == null) {
                return false;
            }
            if (sessionValue.isExpired()) {
                redisTemplate.delete(key);
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    public String getTokenForExistingSession(String email) {
        String key = ACTIVE_ACCESS_TOKEN+":" + email;
        RedisValue<String> sessionValue = (RedisValue<String>) redisTemplate.opsForValue().get(key);
        if (sessionValue == null) {
            return null;
        }
        if (sessionValue.isExpired()) {
            redisTemplate.delete(key);
            return null;
        }
        return sessionValue.getValue();
    }
}
