package com.mongodb.kitchensink.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.kitchensink.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationEntryPoint Tests")
class JwtAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private JwtAuthenticationEntryPoint entryPoint;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    @DisplayName("should set unauthorized status and json response on authentication failure")
    void commence_shouldSetUnauthorizedStatusAndJsonResponse() throws IOException {
        // Given
        AuthenticationException authException = new BadCredentialsException("Invalid token");
        String expectedErrorMessage = "Invalid token";

        // When
        entryPoint.commence(request, response, authException);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        // Use ObjectMapper to verify the JSON content
        ObjectMapper objectMapper = new ObjectMapper();
        ErrorResponse actualResponse = objectMapper.readValue(stringWriter.toString(), ErrorResponse.class);

        // Assertions for the deserialized JSON object
        assertEquals(expectedErrorMessage, actualResponse.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatus());
        assertNotNull(actualResponse.getMessage());
    }
}