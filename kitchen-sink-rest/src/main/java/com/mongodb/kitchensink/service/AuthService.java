package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.dto.LoginResponse;
import com.mongodb.kitchensink.exception.UserAuthException;
import com.mongodb.kitchensink.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;



    @Autowired
    private  JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;


    public LoginResponse login(String email, String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            List<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String username = auth.getName();
            String token = jwtTokenProvider.generateToken(email, roles);

            return new LoginResponse(true, "Login successful", token, email, username, roles);

        } catch (BadCredentialsException | DisabledException e) {
            ErrorCodes errorCode = (e instanceof BadCredentialsException)
                    ? ErrorCodes.INVALID_CREDENTIALS
                    : ErrorCodes.ACCOUNT_DISABLED;
            throw new UserAuthException(errorCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isUserAdminOrUser(List<String> roles) {
        return roles.contains("ROLE_ADMIN") || roles.contains("ROLE_USER");
    }
    public boolean validateSession(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            return false;
        }

        String username = jwtTokenProvider.getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        return userDetails != null;
    }

}
