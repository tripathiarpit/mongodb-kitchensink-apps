package com.mongodb.kitchensink.config;

import com.mongodb.kitchensink.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {


        ErrorResponse errorResponse = new ErrorResponse(authException.getMessage(), HttpStatus.BAD_REQUEST);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

//        Map<String, Object> body = new HashMap<>();
//        body.put("message", authException.getMessage());

        new ObjectMapper().writeValue(response.getWriter(), errorResponse);
    }
}
