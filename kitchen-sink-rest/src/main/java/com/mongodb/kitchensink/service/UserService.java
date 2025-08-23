package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.constants.ErrorCodes;
import com.mongodb.kitchensink.constants.ErrorMessageConstants;
import com.mongodb.kitchensink.constants.UserAccountType;
import com.mongodb.kitchensink.dto.*;
import com.mongodb.kitchensink.exception.AccountVerificationException;
import com.mongodb.kitchensink.exception.UserNotFoundException;
import com.mongodb.kitchensink.mapper.ProfileMapper;
import com.mongodb.kitchensink.mapper.UserMapper;
import com.mongodb.kitchensink.model.Address;
import com.mongodb.kitchensink.model.Profile;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.repository.ProfileRepository;
import com.mongodb.kitchensink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mongodb.kitchensink.constants.AppContants.ACCOUNT_VERIFICATION;
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
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${otp.accountVerification.ttlSeconds}")
    private long accountVerificationTtl;
    @Autowired
    public UserService(OtpService otpService, UserRepository userRepository,
                       ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       ProfileMapper profileMapper,
                       UserMapper userMapper, UsernameGeneratorService usernameGeneratorService,
                       EmailService emailService, EmailService emailService1,
                       RedisTemplate<String, Object> redisTemplate) {
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileMapper = profileMapper;
        this.userMapper = userMapper;
        this.usernameGeneratorService = usernameGeneratorService;
        this.emailService = emailService1;
        this.redisTemplate = redisTemplate;
    }

    public RegistrationResponse registerUser(RegistrationRequest request, Authentication authentication) {
        request.setEmail(request.getEmail().toLowerCase());
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
            roles = List.of(String.valueOf(UserAccountType.USER_ACCOUNT_TYPE.getType()));
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .active(true)
                .accountVerificationPending(true)
                .isFirstLogin(true)
                .twoFactorEnabled(true)
                .twoFactorSecret(null)
                .username(usernameGeneratorService.generateUniqueUsername(request.getEmail()))
                .createdAt(Instant.now())
                .build();
        userRepository.save(user);

        Profile profile = profileMapper.toProfile(request);
        profile.setUsername(user.getUsername());
        profileRepository.save(profile);
        String otp  = otpService.generateOtp(request.getEmail(), ACCOUNT_VERIFICATION, accountVerificationTtl);
        emailService.sendEmail(
                user.getEmail(),
                ACCOUNT_VERIFICATION_SUBJECT,
                String.format(ACCOUNT_VERIFICATION_BODY_TEMPLATE,profile.getFirstName()+" "+profile.getLastName(), otp));

        return new RegistrationResponse(true, ACCOUNT_CREATED_SUCCESSFULLY);
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
    public List<UserDto> getAllUsersByEmailIds(List<String> emailIds) {
        if (emailIds == null || emailIds.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<String> emaildIdSLowerCase = emailIds.stream().map(String::toLowerCase).collect(Collectors.toCollection(ArrayList::new));
        List<User> users = userRepository.findByEmailIn(emaildIdSLowerCase);
        List<UserDto> userDtos = users.stream()
                .map(user -> {
                    Optional<Profile> profileOptional = profileRepository.findByEmail(user.getEmail());
                    UserDto dto = userMapper.toDto(user);
                    profileOptional.ifPresent(profile -> dto.setProfile(userMapper.toDto(profile)));
                    return dto;
                })
                .collect(Collectors.toList());

        return userDtos;
    }

    public UserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new  UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.ACCOUNT_NOT_FOUND_ID));
        return userMapper.toDto(user);
    }

    public UserDto getUserByEmail(String email) {
        if(email == null || email.isEmpty()) {
            throw new UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL);
        }
        email = email.toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        ErrorCodes.RESOURCE_NOT_FOUND,
                        ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL
                ));
        Profile profile = profileRepository.findByEmail(user.getEmail()).orElse(null);
        UserDto dto = userMapper.toDto(user);
        dto.setProfile(profile != null ? userMapper.toDto(profile) : null);

        return dto;
    }
    public User getUserByUserName(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(
                        ErrorCodes.RESOURCE_NOT_FOUND,
                        ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL
                ));


        return user;
    }
    public void saveUser(User user) {
        userRepository.save(user);
    }
    public User getUserByEmailForVerification(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        ErrorCodes.RESOURCE_NOT_FOUND,
                        ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL
                ));
    }
    public Page<UserDto> getUsersByCity(String city, Pageable pageable) {
        return profileRepository.findByAddress_CityContainingIgnoreCase(city, pageable)
                .map(profile -> {
                    User user = userRepository.findByEmail(profile.getEmail())
                            .orElseThrow(() -> new UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.USERS_NOT_FOUND_BY_CITY));
                    return getUserDto(profile, user);
                });
    }

    private UserDto getUserDto(Profile profile, User user) {
        ProfileDto profileDto = profileMapper.toDto(profile);
        if (profile.getAddress() != null) {
            profileDto.setStreet(profile.getAddress().getStreet());
            profileDto.setCity(profile.getAddress().getCity());
            profileDto.setState(profile.getAddress().getState());
            profileDto.setCountry(profile.getAddress().getCountry());
            profileDto.setPincode(profile.getAddress().getPincode());
        }
        UserDto dto = userMapper.toDto(user);
        dto.setProfile(profileDto);
        return dto;
    }

    public void activateAccount(String email,boolean firstLogin) {
        if(email == null || email.isEmpty()) {
            throw new UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL);
        }
        email = email.toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new  UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL));
        if(!user.getAccountVerificationPending() && !firstLogin) {
            throw new AccountVerificationException(ErrorCodes.ACCOUNT_VERIFICATION_FAILED, ErrorMessageConstants.ACCOUNT_VERFIED);
        }
        user.setAccountVerificationPending(false);
        userRepository.save(user);
    }
    @Transactional
    public ResourceDeleteResponse deleteUserByEmail(String email) {
        if(email==null || email.isEmpty()) {
            throw new UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL);
        }
        email = email.toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        ErrorCodes.RESOURCE_NOT_FOUND,
                        ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL
                ));


        Profile profile = profileRepository.findByEmail(email).orElse(null);
        userRepository.delete(user);
        if (profile != null) {
            profileRepository.delete(profile);
        }
        String redisKey = "USER::" + email;
        redisTemplate.delete(redisKey);
        return new ResourceDeleteResponse(true, ACCOUNT_DELETED_SUCCESSFULLY);
    }
    public void saveUpdatedUser(UserDto userDto) throws Exception{
        User user = userMapper.toEntity(userDto);
        userRepository.save(user);
    }
    public void saveUpdatedUserAfterVerification(User user) throws Exception{
        userRepository.save(user);
    }
    public Page<UserDto> getUsersByName(String name, Pageable pageable) {
        Page<Profile> profiles = profileRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name, pageable);

        if (profiles.isEmpty()) {
            throw new UserNotFoundException(
                    ErrorCodes.RESOURCE_NOT_FOUND,
                    ErrorMessageConstants.USERS_NOT_FOUND_BY_NAME
            );
        }

        return profiles.map(profile -> {
            User user = userRepository.findByUsername(profile.getUsername())
                    .orElseThrow(() -> new UserNotFoundException(
                            ErrorCodes.RESOURCE_NOT_FOUND,
                            ErrorMessageConstants.USERS_NOT_FOUND_BY_USER_ID
                    ));
            return getUserDto(profile, user);
        });
    }

    public Page<UserDto> getUsersByEmail(String email, Pageable pageable) {
        return userRepository.findByEmailContainingIgnoreCase(email, pageable)
                .map(user -> {
                    Profile profile = profileRepository.findByEmail(user.getEmail()).orElseThrow(() -> new UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.USER_NOT_FOUND_EMAIL));
                    UserDto dto = userMapper.toDto(user);
                    dto.setProfile(profile != null ? profileMapper.toDto(profile) : null);
                    return dto;
                });
    }

    public Page<UserDto> getUsersByCountry(String country, Pageable pageable) {
        Page<Profile> profiles = profileRepository.findByAddress_CountryContainingIgnoreCase(country, pageable);
        if (profiles.isEmpty()) {
            throw new UserNotFoundException(
                    ErrorCodes.RESOURCE_NOT_FOUND,
                    ErrorMessageConstants.USERS_NOT_FOUND_BY_COUNTRY
            );
        }

        return profiles.map(profile -> {
            User user = userRepository.findByUsername(profile.getUsername())
                    .orElseThrow(() -> new UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.USERS_NOT_FOUND_BY_USER_ID));
            return getUserDto(profile, user);
        });
    }

    @Transactional
    public UserDto updateUser(String emailId, UserDto updateRequest) {
        if(emailId == null || emailId.isEmpty()) {
            throw new UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.ACCOUNT_NOT_FOUND_EMAIL);
        }
        emailId = emailId.toLowerCase();
        User existingUser = userRepository.findByEmail(emailId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.USERS_NOT_FOUND_BY_EMAIL));
        if (existingUser == null) {
            throw new AccountVerificationException(ErrorCodes.VALIDATION_ERROR, ErrorMessageConstants.USERS_NOT_FOUND_BY_EMAIL);
        }
        Profile existingProfile = profileRepository.findByUsername(existingUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, ErrorMessageConstants.USERS_NOT_FOUND_BY_EMAIL));
        if (existingProfile == null) {
            throw new AccountVerificationException(ErrorCodes.VALIDATION_ERROR, ErrorMessageConstants.USERS_NOT_FOUND_BY_USER_ID);
        }
        if (updateRequest.getUsername() != null && !updateRequest.getUsername().equals(existingUser.getUsername())) {
            Optional<Profile> profileExist = profileRepository.findByUsername(updateRequest.getUsername());
            if (!profileExist.isEmpty()) {
                throw new AccountVerificationException(ErrorCodes.VALIDATION_ERROR, ErrorMessageConstants.USERS_FOUND_BY_USER_ID);
            }
            existingUser.setUsername(updateRequest.getUsername());
            existingProfile.setUsername(updateRequest.getUsername());
        }
        if (updateRequest.getRoles() != null)
            existingUser.setRoles(updateRequest.getRoles());

        existingUser.setActive(updateRequest.isActive());
        if(updateRequest.getRoles() != null) {
            existingUser.setRoles(updateRequest.getRoles());
        }
        if (updateRequest.getFirstLogin() != null) existingUser.setFirstLogin(updateRequest.getFirstLogin());
        if (updateRequest.getAccountVerificationPending() != null)
            existingUser.setAccountVerificationPending(updateRequest.getAccountVerificationPending());

        // 4. Update Profile fields
        ProfileDto profileDto = updateRequest.getProfile();
        if (profileDto != null) {
            if (profileDto.getFirstName() != null) existingProfile.setFirstName(profileDto.getFirstName());
            if (profileDto.getLastName() != null) existingProfile.setLastName(profileDto.getLastName());
            if (profileDto.getPhoneNumber() != null) existingProfile.setPhoneNumber(profileDto.getPhoneNumber());

            if (profileDto.getStreet() != null || profileDto.getCity() != null
                    || profileDto.getState() != null || profileDto.getCountry() != null
                    || profileDto.getPincode() != null) {

                Address existingAddress = existingProfile.getAddress() != null ? existingProfile.getAddress() : new Address();

                if (profileDto.getStreet() != null) existingAddress.setStreet(profileDto.getStreet());
                if (profileDto.getCity() != null) existingAddress.setCity(profileDto.getCity());
                if (profileDto.getState() != null) existingAddress.setState(profileDto.getState());
                if (profileDto.getCountry() != null) existingAddress.setCountry(profileDto.getCountry());
                if (profileDto.getPincode() != null) existingAddress.setPincode(profileDto.getPincode());

                existingProfile.setAddress(existingAddress);
            }
        }

        profileRepository.save(existingProfile);
        userRepository.save(existingUser);
        UserDto updatedDto = new UserDto();
        updatedDto.setId(existingUser.getId());
        updatedDto.setEmail(existingUser.getEmail());
        updatedDto.setUsername(existingUser.getUsername());
        updatedDto.setRoles(existingUser.getRoles());
        updatedDto.setActive(existingUser.isActive());
        updatedDto.setFirstLogin(existingUser.getFirstLogin());
        updatedDto.setAccountVerificationPending(existingUser.getAccountVerificationPending());
        updatedDto.setCreatedAt(existingUser.getCreatedAt());
        ProfileDto updatedProfile = new ProfileDto();
        updatedProfile.setFirstName(existingProfile.getFirstName());
        updatedProfile.setLastName(existingProfile.getLastName());
        updatedProfile.setPhoneNumber(existingProfile.getPhoneNumber());
        if (existingProfile.getAddress() != null) {
            updatedProfile.setStreet(existingProfile.getAddress().getStreet());
            updatedProfile.setCity(existingProfile.getAddress().getCity());
            updatedProfile.setState(existingProfile.getAddress().getState());
            updatedProfile.setCountry(existingProfile.getAddress().getCountry());
            updatedProfile.setPincode(existingProfile.getAddress().getPincode());
        }
        updatedDto.setProfile(updatedProfile);
        return updatedDto;
    }


    private String extractUsernameFromEmail(String email) {
        if (email != null && email.contains("@")) return email.substring(0, email.indexOf("@"));
        return email;
    }
}
