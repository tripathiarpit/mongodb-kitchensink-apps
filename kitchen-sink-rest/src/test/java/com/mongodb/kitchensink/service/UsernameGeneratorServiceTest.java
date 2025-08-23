package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsernameGeneratorService Tests")
class UsernameGeneratorServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecureRandom random;

    @InjectMocks
    private UsernameGeneratorService usernameGeneratorService;

    private final String EMAIL = "TestUser@Example.com";
    private final String BASE_USERNAME = "testuser";
    @BeforeEach
    void setUp() {

    }

    // --- generateUniqueUsername Tests ---

    @Test
    @DisplayName("should generate base username when it is unique")
    void generateUniqueUsername_shouldGenerateBaseUsername_WhenUnique() {
        // Given
        when(userRepository.existsByUsername(BASE_USERNAME)).thenReturn(false);

        // When
        String result = usernameGeneratorService.generateUniqueUsername(EMAIL);

        // Then
        assertEquals(BASE_USERNAME, result);
        verify(userRepository, times(1)).existsByUsername(BASE_USERNAME);
        verify(random, never()).nextInt(anyInt());
    }


    @Test
    @DisplayName("should handle email with multiple '@' symbols")
    void generateUniqueUsername_shouldHandleEmailWithMultipleAtSymbols() {
        // Given
        String emailWithMultipleAt = "test.user@sub.domain@example.com";
        String expectedBaseUsername = "test.user";
        when(userRepository.existsByUsername(expectedBaseUsername)).thenReturn(false);

        // When
        String result = usernameGeneratorService.generateUniqueUsername(emailWithMultipleAt);

        // Then
        assertEquals(expectedBaseUsername, result);
        verify(userRepository, times(1)).existsByUsername(expectedBaseUsername);
    }
}