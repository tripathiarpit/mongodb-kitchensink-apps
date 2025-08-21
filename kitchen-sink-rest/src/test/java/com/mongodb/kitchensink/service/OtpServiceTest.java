package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.RedisValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OtpService Tests")
class OtpServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SecureRandom secureRandom;

    @InjectMocks
    private OtpService otpService;

    private final String EMAIL = "test@example.com";
    private final String OTP_TYPE = "ACCOUNT_VERIFICATION";
    private final String REDIS_KEY = "OTP:ACCOUNT_VERIFICATION:test@example.com";

    @BeforeEach
    void setUp() {
        // Use lenient() to suppress UnnecessaryStubbingException for this shared setup
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        ReflectionTestUtils.setField(otpService, "secureRandom", secureRandom);
        ReflectionTestUtils.setField(otpService, "accountVerificationTtl", 600L);
        ReflectionTestUtils.setField(otpService, "forgotPasswordTtl", 900L);
    }

    // --- generateOtp Tests ---

    @Test
    @DisplayName("generateOtp should return existing OTP when not expired")
    void generateOtp_shouldReturnExistingOtpWhenNotExpired() {
        // Given
        RedisValue<String> existingValue = mock(RedisValue.class);
        when(existingValue.isValid()).thenReturn(true);
        when(existingValue.getValue()).thenReturn("123456");
        when(valueOperations.get(REDIS_KEY)).thenReturn(existingValue);

        // When
        String result = otpService.generateOtp(EMAIL, OTP_TYPE, null);

        // Then
        assertEquals("123456", result);
        verify(secureRandom, never()).nextInt(anyInt());
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("generateOtp should generate new OTP when no existing OTP")
    void generateOtp_shouldGenerateNewOtpWhenNoExistingOtp() {
        // Given
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(secureRandom.nextInt(anyInt())).thenReturn(123456);

        // When
        String result = otpService.generateOtp(EMAIL, OTP_TYPE, null);

        // Then
        assertEquals("123456", result);
        verify(valueOperations, times(1)).set(eq(REDIS_KEY), any(RedisValue.class), eq(600L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("generateOtp should generate new OTP when existing OTP is expired")
    void generateOtp_shouldGenerateNewOtpWhenExistingOtpIsExpired() {
        // Given
        RedisValue<String> existingValue = mock(RedisValue.class);
        when(existingValue.isValid()).thenReturn(false);
        when(valueOperations.get(REDIS_KEY)).thenReturn(existingValue);
        when(secureRandom.nextInt(anyInt())).thenReturn(123456);

        // When
        String result = otpService.generateOtp(EMAIL, OTP_TYPE, null);

        // Then
        assertEquals("123456", result);
        verify(valueOperations, times(1)).set(eq(REDIS_KEY), any(RedisValue.class), eq(600L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("generateOtp should use provided TTL when not null")
    void generateOtp_shouldUseProvidedTtlWhenNotNull() {
        // Given
        long customTtl = 120L; // 2 minutes
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(secureRandom.nextInt(anyInt())).thenReturn(123456);

        // When
        otpService.generateOtp(EMAIL, OTP_TYPE, customTtl);

        // Then
        verify(valueOperations, times(1)).set(eq(REDIS_KEY), any(RedisValue.class), eq(customTtl), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("generateOtp should use default TTL for 'forgotPassword' type")
    void generateOtp_shouldUseDefaultTtlForForgotPasswordType() {
        // Given
        String forgotPasswordType = "FORGOT_PASSWORD";
        String forgotPasswordKey = "OTP:FORGOT_PASSWORD:test@example.com";
        when(valueOperations.get(forgotPasswordKey)).thenReturn(null);
        when(secureRandom.nextInt(anyInt())).thenReturn(123456);

        // When
        otpService.generateOtp(EMAIL, forgotPasswordType, null);

        // Then
        verify(valueOperations, times(1)).set(eq(forgotPasswordKey), any(RedisValue.class), eq(900L), eq(TimeUnit.SECONDS));
    }

    // --- verifyOtp Tests ---

    @Test
    @DisplayName("verifyOtp should return true and clear key for valid OTP")
    void verifyOtp_shouldReturnTrueAndClearKeyForValidOtp() {
        // Given
        String otp = "123456";
        RedisValue<String> storedValue = mock(RedisValue.class);
        when(storedValue.isValid()).thenReturn(true);
        when(storedValue.getValue()).thenReturn(otp);
        when(valueOperations.get(REDIS_KEY)).thenReturn(storedValue);

        // When
        boolean isValid = otpService.verifyOtp(EMAIL, OTP_TYPE, otp);

        // Then
        assertTrue(isValid);
        verify(redisTemplate, times(1)).delete(REDIS_KEY);
    }

    @Test
    @DisplayName("verifyOtp should return false for invalid OTP")
    void verifyOtp_shouldReturnFalseForInvalidOtp() {
        // Given
        String correctOtp = "123456";
        String invalidOtp = "654321";
        RedisValue<String> storedValue = mock(RedisValue.class);
        when(storedValue.isValid()).thenReturn(true);
        when(storedValue.getValue()).thenReturn(correctOtp);
        when(valueOperations.get(REDIS_KEY)).thenReturn(storedValue);

        // When
        boolean isValid = otpService.verifyOtp(EMAIL, OTP_TYPE, invalidOtp);

        // Then
        assertFalse(isValid);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("verifyOtp should return false when stored OTP is expired")
    void verifyOtp_shouldReturnFalseWhenStoredOtpIsExpired() {
        // Given
        String otp = "123456";
        RedisValue<String> storedValue = mock(RedisValue.class);
        when(storedValue.isValid()).thenReturn(false);
        when(valueOperations.get(REDIS_KEY)).thenReturn(storedValue);

        // When
        boolean isValid = otpService.verifyOtp(EMAIL, OTP_TYPE, otp);

        // Then
        assertFalse(isValid);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("verifyOtp should return false when no stored OTP found")
    void verifyOtp_shouldReturnFalseWhenNoStoredOtpFound() {
        // Given
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);

        // When
        boolean isValid = otpService.verifyOtp(EMAIL, OTP_TYPE, "123456");

        // Then
        assertFalse(isValid);
        verify(redisTemplate, never()).delete(anyString());
    }

    // --- clearOtp Test ---

    @Test
    @DisplayName("clearOtp should delete the OTP key from Redis")
    void clearOtp_shouldDeleteOtpKeyFromRedis() {
        // When
        otpService.clearOtp(EMAIL, OTP_TYPE);

        // Then
        verify(redisTemplate, times(1)).delete(REDIS_KEY);
    }
}