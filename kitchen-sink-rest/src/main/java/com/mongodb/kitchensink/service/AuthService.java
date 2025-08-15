package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.ErrorMessageConstants;
import com.mongodb.kitchensink.dto.LoginResponse;
import com.mongodb.kitchensink.dto.ProfileDto;
import com.mongodb.kitchensink.exception.InvalidRequestException;
import com.mongodb.kitchensink.exception.JwtExpiredException;
import com.mongodb.kitchensink.exception.UserAuthException;
import com.mongodb.kitchensink.exception.UserNotFoundException;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mongodb.kitchensink.constants.ErrorCodes.USER_NOT_FOUND;
import static com.mongodb.kitchensink.constants.ErrorMessageConstants.*;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private  JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired UserService userService;

    @Autowired
    private SessionService sessionService;

    public LoginResponse login(String email, String password) {
        try {
            var user = userService.getUserByEmail(email);
            if (user == null) {
                throw new UserNotFoundException(ErrorCodes.USER_NOT_FOUND, ACCOUNT_NOT_FOUND_EMAIL);
            }

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            List<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String username = auth.getName();
            String token;

            if (sessionService.doesSessionExist(email)) {
                token = sessionService.getTokenForExistingSession(email); // reuse existing token
            } else {
                token = jwtTokenProvider.generateToken(email, roles);
                sessionService.storeSessionToken(email, token); // store new session
            }
            String firstName = Optional.ofNullable(user.getProfile())
                    .map(ProfileDto::getFirstName)
                    .orElse("");

            String lastName = Optional.ofNullable(user.getProfile())
                    .map(ProfileDto::getLastName)
                    .orElse("");

            String fullName = (firstName + " " + lastName).trim();
            return new LoginResponse(true, "Login successful", token, email, username, fullName, roles);

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
            throw new InvalidRequestException(ErrorCodes.VALIDATION_ERROR, INVALID_REQUEST);
        }
        String token = authHeader.substring(7);
        jwtTokenProvider.validateToken(token);
        String username = jwtTokenProvider.getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return userDetails != null;
    }
    public List<String> getRolesFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_ERROR, INVALID_REQUEST);
        }
        String token = authHeader.substring(7);
        jwtTokenProvider.validateToken(token);
        String email = jwtTokenProvider.getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        return userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
    public List<String> getRolesByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new UserNotFoundException(ErrorCodes.USER_NOT_FOUND, EMAIL_REQUIRED);
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (userDetails == null) {
            throw new UserNotFoundException(ErrorCodes.VALIDATION_ERROR, INVALID_REQUEST);
        }
        return userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

}
