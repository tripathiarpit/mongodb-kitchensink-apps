package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.ErrorMessageConstants;
import com.mongodb.kitchensink.dto.RegistrationRequest;
import com.mongodb.kitchensink.dto.RegistrationResponse;
import com.mongodb.kitchensink.dto.UserDto;
import com.mongodb.kitchensink.exception.ResourceNotFoundException;
import com.mongodb.kitchensink.exception.UserNotFoundException;
import com.mongodb.kitchensink.mapper.ProfileMapper;
import com.mongodb.kitchensink.mapper.UserMapper;
import com.mongodb.kitchensink.model.Profile;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.repository.ProfileRepository;
import com.mongodb.kitchensink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static com.mongodb.kitchensink.constants.SuccessMessageConstants.*;

@Service
public class UserService {
    private  final OtpService otpService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileMapper profileMapper;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final UsernameGeneratorService usernameGeneratorService;
    @Autowired
    public UserService(OtpService otpService, UserRepository userRepository,
                       ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       ProfileMapper profileMapper,
                       UserMapper userMapper, UsernameGeneratorService usernameGeneratorService,
                       EmailService emailService, EmailService emailService1) {
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileMapper = profileMapper;
        this.userMapper = userMapper;
        this.usernameGeneratorService = usernameGeneratorService;
        this.emailService = emailService1;
    }

    public RegistrationResponse registerUser(RegistrationRequest request, Authentication authentication) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return new RegistrationResponse(false, "Email already registered");
        }

        boolean isAdmin = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<String> roles;
        if (isAdmin && request.getRoles() != null && !request.getRoles().isEmpty()) {
            roles = request.getRoles();
        } else {
            roles = List.of("USER");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .active(true)
                .username(usernameGeneratorService.generateUniqueUsername(request.getEmail()))
                .createdAt(Instant.now())
                .build();
        userRepository.save(user);

        Profile profile = profileMapper.toProfile(request);
        profile.setUsername(user.getUsername());
        profileRepository.save(profile);
        String otp  = otpService.generateOtp(request.getEmail(), "ACCOUNT_VERIFICATION", 2 * 60 * 60);
        emailService.sendEmail(
                user.getEmail(),
                ACCOUNT_VERIFICATION_SUBJECT,
                String.format(ACCOUNT_VERIFICATION_BODY_TEMPLATE,profile.getFirstName()+" "+profile.getLastName(), otp));

        return new RegistrationResponse(true, "User registered successfully");
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> {
                    Profile profile = profileRepository.findByEmail(user.getEmail())
                            .orElse(null);
                    UserDto dto = userMapper.toDto(user);
                    dto.setProfile(profile != null ? userMapper.toDto(profile) : null);
                    return dto;
                });
    }

    public UserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new  UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.ACCOUNT_NOT_FOUND_ID));
        return userMapper.toDto(user);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new  UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL));

        Profile profile = profileRepository.findByEmail(user.getEmail())
                .orElse(null);
        UserDto dto = userMapper.toDto(user);
        dto.setProfile(profile != null ? userMapper.toDto(profile) : null);
        return dto;
    }

    public Page<UserDto> getUsersByCity(String city, Pageable pageable) {
        return profileRepository.findByAddress_CityIgnoreCase(city, pageable)
                .map(profile -> {
                    User user = userRepository.findById(profile.getUsername())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "User not found for profile with ID " + profile.getId()
                            ));
                    UserDto dto = userMapper.toDto(user);
                    dto.setProfile(profileMapper.toDto(profile));

                    return dto;
                });
    }
}
