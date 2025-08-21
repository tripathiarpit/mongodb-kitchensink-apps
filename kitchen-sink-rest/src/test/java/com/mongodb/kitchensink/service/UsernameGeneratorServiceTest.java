package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsernameGeneratorService Tests")
class UsernameGeneratorServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Random random;

    @InjectMocks
    private UsernameGeneratorService usernameGeneratorService;

    private final String EMAIL = "TestUser@Example.com";
    private final String BASE_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(usernameGeneratorService, "random", random);
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
    @DisplayName("should generate username with suffix when base username exists")
    void generateUniqueUsername_shouldGenerateUsernameWithSuffix_WhenBaseExists() {
        // Given
        when(userRepository.existsByUsername(BASE_USERNAME)).thenReturn(true);
        when(userRepository.existsByUsername(BASE_USERNAME + "abcd")).thenReturn(false);
        when(random.nextInt(anyInt())).thenReturn(0, 1, 2, 3); // Mocks the suffix "abcd"

        // When
        String result = usernameGeneratorService.generateUniqueUsername(EMAIL);

        // Then
        assertEquals(BASE_USERNAME + "abcd", result);
        verify(userRepository, times(1)).existsByUsername(BASE_USERNAME);
        verify(userRepository, times(1)).existsByUsername(BASE_USERNAME + "abcd");
        verify(random, times(4)).nextInt(anyInt());
    }

    @Test
    @DisplayName("should handle multiple collisions before finding unique username")
    void generateUniqueUsername_shouldHandleMultipleCollisions() {
        // Given
        when(userRepository.existsByUsername(BASE_USERNAME)).thenReturn(true);
        when(userRepository.existsByUsername(BASE_USERNAME + "aaaa")).thenReturn(true);
        when(userRepository.existsByUsername(BASE_USERNAME + "aaab")).thenReturn(true);
        when(userRepository.existsByUsername(BASE_USERNAME + "aaac")).thenReturn(false);

        // The sequence of random numbers to generate suffixes "aaaa", "aaab", and "aaac"
        when(random.nextInt(anyInt())).thenReturn(0, 0, 0, 0, // for "aaaa"
                0, 0, 0, 1, // for "aaab"
                0, 0, 0, 2); // for "aaac"

        // When
        String result = usernameGeneratorService.generateUniqueUsername(EMAIL);

        // Then
        assertEquals(BASE_USERNAME + "aaac", result);
        verify(userRepository, times(1)).existsByUsername(BASE_USERNAME);
        verify(userRepository, times(1)).existsByUsername(BASE_USERNAME + "aaaa");
        verify(userRepository, times(1)).existsByUsername(BASE_USERNAME + "aaab");
        verify(userRepository, times(1)).existsByUsername(BASE_USERNAME + "aaac");
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