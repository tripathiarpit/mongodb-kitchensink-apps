package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.dto.RegistrationRequest;
import com.mongodb.kitchensink.dto.RegistrationResponse;
import com.mongodb.kitchensink.exception.ResourceNotFoundException;
import com.mongodb.kitchensink.mapper.ProfileMapper;
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
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileMapper profileMapper;

    @Autowired
    public UserService(UserRepository userRepository,
                       ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       ProfileMapper profileMapper) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileMapper = profileMapper;
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
                .createdAt(Instant.now())
                .build();
        userRepository.save(user);

        Profile profile = profileMapper.toProfile(request);
        profile.setUserId(user.getId());
        profileRepository.save(profile);

        return new RegistrationResponse(true, "User registered successfully");
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));
    }

    public Page<Profile> getUsersByCity(String city, Pageable pageable) {
        return profileRepository.findByAddress_CityIgnoreCase(city, pageable);
    }
}
