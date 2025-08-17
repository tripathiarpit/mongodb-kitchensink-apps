package com.mongodb.kitchensink.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.kitchensink.service.TwoFactorAuthService;
import com.mongodb.kitchensink.service.UserService;
import com.mongodb.kitchensink.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

public class TwoFactorAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final TwoFactorAuthService twoFactorService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public TwoFactorAuthenticationFilter(AuthenticationManager authenticationManager,
                                         TwoFactorAuthService twoFactorService,
                                         UserService userService) {
        super(authenticationManager);
        this.twoFactorService = twoFactorService;
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException {

        try {
            String username = null;
            String password = null;
            String totpCode = null;

            // Handle both JSON and form data
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                // Parse JSON request body
                Map<String, String> credentials = objectMapper.readValue(
                        request.getInputStream(),
                        Map.class
                );
                username = credentials.get("username");
                password = credentials.get("password");
                totpCode = credentials.get("totp");
            } else {
                // Handle form data (original behavior)
                username = obtainUsername(request);
                password = obtainPassword(request);
                totpCode = request.getParameter("totp");
            }

            if (username == null) username = "";
            if (password == null) password = "";

            // Create authentication token for username/password
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(username, password);

            // First, authenticate with username/password
            Authentication auth = this.getAuthenticationManager().authenticate(authRequest);

            // Then check 2FA if enabled
            User user = userService.getUserByUserName(username);
            if (user != null && user.isTwoFactorEnabled()) {
                if (totpCode == null || totpCode.trim().isEmpty()) {
                    throw new BadCredentialsException("2FA code is required");
                }

                boolean isValidTOTP = twoFactorService.verifyCode(user.getTwoFactorSecret(), totpCode);
                if (!isValidTOTP) {
                    throw new BadCredentialsException("Invalid 2FA code");
                }
            }

            return auth;

        } catch (IOException e) {
            throw new BadCredentialsException("Invalid request format", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException, ServletException {

        super.successfulAuthentication(request, response, chain, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String message = "Authentication failed";
        if (failed.getMessage() != null) {
            message = failed.getMessage();
        }

        String jsonResponse = String.format(
                "{\"error\": \"Unauthorized\", \"message\": \"%s\"}",
                message
        );

        response.getWriter().write(jsonResponse);
    }
}