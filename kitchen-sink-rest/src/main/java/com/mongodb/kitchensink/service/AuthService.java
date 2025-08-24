package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.SuccessMessageConstants;
import com.mongodb.kitchensink.dto.*;
import com.mongodb.kitchensink.exception.*;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.util.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import static com.mongodb.kitchensink.constants.AppContants.*;
import static com.mongodb.kitchensink.constants.ErrorMessageConstants.*;
import static com.mongodb.kitchensink.constants.SuccessMessageConstants.*;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final SessionService sessionService;
    private final JwtTokenProvider jwtTokenProvider;
    @Value("${otp.accountVerification.ttlSeconds}")
    private  long accountVerificationTtl;

    @Value("${otp.forgotPassword.ttlSeconds}")
    private long forgotPasswordTtl;
    @Value("${jwt.refresh-expiration-seconds}")
    private long refreshTokenExpirationSeconds;

    @Value("${app.session.expiration-seconds}")
    private long accessTokenExpirationSeconds;


    public AuthService(
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            UserService userService,
            OtpService otpService,
            EmailService emailService,
            SessionService sessionService,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.otpService = otpService;
        this.emailService = emailService;
        this.sessionService = sessionService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest loginRequest) throws UserAuthException, UserNotFoundException, Exception {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        if(password == null || password.isEmpty()) {
            throw new InvalidRequestException(ErrorCodes.INVALID_CREDENTIALS);
        }
        try {
            email = email.toLowerCase();
            UserDto user = userService.getUserByEmail(email);
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            List<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            String fullName = getFullName(user);

            if (user.getAccountVerificationPending() && user.getFirstLogin()) {
                User currentUser = userService.getUserByEmailForVerification(email);
                currentUser.setFirstLogin(false);
                userService.saveUpdatedUserAfterVerification(currentUser);
                UserDto updatedUser = userService.getUserByEmail(email);
                return new LoginResponse(true, LOGGED_IN_SUCCESSFULLY, null,null, email, auth.getName(),
                        fullName, roles, updatedUser.getAccountVerificationPending(), updatedUser.getFirstLogin());
            }
            if(!user.isActive()) {
                throw new UserAuthException(ErrorCodes.ACCOUNT_DISABLED, USERS_ACCOUNT_DISABLED);
            }
            String accessToken = jwtTokenProvider.generateAccessToken(email, roles);
            String refreshToken = jwtTokenProvider.generateRefreshToken(email);

            sessionService.storeRefreshToken(email, refreshToken, refreshTokenExpirationSeconds);
            sessionService.storeAccessToken(email, accessToken,accessTokenExpirationSeconds);
            return new LoginResponse(true, "Login successful", accessToken,refreshToken, email, auth.getName(),
                    fullName, roles, user.getAccountVerificationPending(), user.getFirstLogin());
        }
        catch (BadCredentialsException | DisabledException e) {
            throw mapAuthException(e);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (UserAuthException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error", e);
        }
    }

    public void validateLoginRequest(LoginRequest loginRequest) throws InvalidRequestException, BadRequestException {
        if (loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
            throw new BadRequestException(ErrorCodes.VALIDATION_ERROR, REQ_PASSWORD);
        }

        if (loginRequest.getEmail() == null || loginRequest.getEmail().isBlank()) {
            throw new BadRequestException(ErrorCodes.VALIDATION_ERROR, REQ_EMAIL);
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!loginRequest.getEmail().matches(emailRegex)) {
            throw new BadRequestException(ErrorCodes.VALIDATION_ERROR, INVALID_EMAIL_FORMET);
        }
    }
    public LoginResponse login(String email, String password) throws UserAuthException, UserNotFoundException, Exception {
        return this.login(new LoginRequest(email, password));
    }
    public void logout(String email) throws UserAuthException, UserNotFoundException, Exception {
        userService.getUserByEmail(email);
        if (sessionService.doesSessionExist(email)) {
            sessionService.invalidateSession(email);
        }
    }

    private UserAuthException mapAuthException(Exception e) {
        ErrorCodes errorCode = (e instanceof BadCredentialsException)
                ? ErrorCodes.INVALID_CREDENTIALS
                : ErrorCodes.ACCOUNT_DISABLED;
        return new UserAuthException(errorCode);
    }


    private UserDto getUserByEmailOrThrow(String email) {
        UserDto user = userService.getUserByEmail(email);
        if (user == null) {
            throw new UserNotFoundException(ErrorCodes.USER_NOT_FOUND, ACCOUNT_NOT_FOUND_EMAIL);
        }
        return user;
    }

    private Authentication authenticateUser(String email, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
    }

    private List<String> extractRoles(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private String getFullName(UserDto user) {
        String firstName = Optional.ofNullable(user.getProfile())
                .map(ProfileDto::getFirstName)
                .orElse("");
        String lastName = Optional.ofNullable(user.getProfile())
                .map(ProfileDto::getLastName)
                .orElse("");
        return (firstName + " " + lastName).trim();
    }

    private UserAuthException handleAuthException(Exception e) {
        ErrorCodes errorCode = (e instanceof BadCredentialsException)
                ? ErrorCodes.INVALID_CREDENTIALS
                : ErrorCodes.ACCOUNT_DISABLED;
        return new UserAuthException(errorCode);
    }

    public boolean isUserAdminOrUser(List<String> roles) {
        return roles.contains(ROLE_ADMIN) || roles.contains(ROLE_USER);
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

    public void sendOtpForAccountVerification(String email) throws UserAuthException, Exception {
        UserDto userDto = userService.getUserByEmail(email);
        String otp = otpService.generateOtp(email, ACCOUNT_VERIFICATION, accountVerificationTtl);
        emailService.sendEmail(
                userDto.getEmail(),
                ACCOUNT_VERIFICATION_SUBJECT,
                String.format(ACCOUNT_VERIFICATION_BODY_TEMPLATE,userDto.getProfile().getFirstName()+" "+userDto.getProfile().getLastName(), otp));
    }
    public ApiResponse verifyOtpForAccountVerification(OtpRequest request) throws UserAuthException, Exception {
        if(request== null) {
            throw new InvalidOtpException(ErrorCodes.ACCOUNT_VERIFICATION_FAILED);
        }
        if(request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new InvalidOtpException(ErrorCodes.ACCOUNT_VERIFICATION_FAILED);
        }
        if(request.getOtp() == null || request.getOtp().isEmpty()) {
            throw new InvalidOtpException(ErrorCodes.ACCOUNT_VERIFICATION_FAILED);
        }
        boolean valid = otpService.verifyOtp(request.getEmail(), ACCOUNT_VERIFICATION, request.getOtp());
        if (!valid) {
            throw new InvalidOtpException(ErrorCodes.ACCOUNT_VERIFICATION_FAILED);
        }
        userService.activateAccount(request.getEmail(), true);
        return new ApiResponse(OTP_VERIFIED_SUCCESS, true);
    }

    public LoginResponse getLoginResponse(String email) {
        UserDto userDto = userService.getUserByEmail(email);
        if(!userDto.getAccountVerificationPending() && !userDto.getFirstLogin()) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String token = jwtTokenProvider.generateAccessToken(email, roles);
            String refreshToken = jwtTokenProvider.generateRefreshToken(email);
            String fullName = getFullName(userDto);
            return new LoginResponse(
                    true,
                    LOGGED_IN_SUCCESSFULLY,
                    token,
                    refreshToken,
                    email,
                    userDetails.getUsername(),
                    fullName,
                    roles,
                    userDto.getAccountVerificationPending(),
                    userDto.getFirstLogin()
            );
        } else {
            throw new AccountVerificationException(ErrorCodes.ACCOUNT_VERIFICATION_PENDING);
        }

    }
    public boolean validateSession(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_ERROR, INVALID_REQUEST);
        }
        String token = authHeader.substring(7);
        jwtTokenProvider.validateAccessToken(token);
        String username = jwtTokenProvider.getEmailFromAccessToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return userDetails != null;
    }
    public List<String> getRolesFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_ERROR, INVALID_REQUEST);
        }
        String token = authHeader.substring(7);
        try {
            jwtTokenProvider.validateAccessToken(token);
            return jwtTokenProvider.getRolesFromToken(token);
        } catch (ExpiredJwtException ex) {

            throw new InvalidRequestException(ErrorCodes.VALIDATION_ERROR, TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_ERROR, TOKEN_INVALID);
        }
    }
    public JwtAuthenticationResponse refreshTokens(String refreshToken) throws JwtExpiredException, AccountVerificationException {
        if (refreshToken == null) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_ERROR, REFRESH_TOKEN_MISSING);
        }

        try {
            jwtTokenProvider.validateRefreshToken(refreshToken);
            String email = jwtTokenProvider.getEmailFromRefreshToken(refreshToken);
            boolean isTokenValid = sessionService.validateAndConsumeRefreshToken(email, refreshToken);
            if (!isTokenValid) {
                throw new AccountVerificationException(ErrorCodes.SESSION_EXPIRED, ANOTHER_SESSION_STARTED);
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            String newAccessToken = jwtTokenProvider.generateAccessToken(email, roles);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);
            sessionService.storeRefreshToken(email, newRefreshToken, refreshTokenExpirationSeconds);
            return new JwtAuthenticationResponse(newAccessToken, newRefreshToken);

        } catch (ExpiredJwtException ex) {
            throw new JwtExpiredException(ErrorCodes.SESSION_EXPIRED, TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token.", e);
        }
    }
}