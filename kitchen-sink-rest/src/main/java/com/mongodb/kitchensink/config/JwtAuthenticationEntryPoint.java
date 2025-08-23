package com.mongodb.kitchensink.config;

import com.mongodb.kitchensink.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;


import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class to Handle Authentication Errors
 * <p>
 *It's primary purpose is to handle / intercept all the request when user tries to access protected resources
 * without a valid JWT TOKEN. Token can be expired, no token or malformed
 * </p>
 * @author Arpit Tripathi
 * @version 1.0
 * @since 2025-08-17
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(authException.getMessage(), HttpStatus.BAD_REQUEST);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), errorResponse);
    }
}
