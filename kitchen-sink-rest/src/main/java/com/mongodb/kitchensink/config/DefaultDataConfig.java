package com.mongodb.kitchensink.config;

import com.mongodb.kitchensink.model.Profile;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.repository.ProfileRepository;
import com.mongodb.kitchensink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class DefaultDataConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DefaultDataConfig(UserRepository userRepository, PasswordEncoder passwordEncoder, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileRepository =  profileRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User adminUser = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .roles(List.of("ADMIN"))
                    .active(true)
                    .createdAt(Instant.now())
                    .build();
            System.out.println("adminUser*********" +
                    "*******##############"+adminUser.getUsername());
            System.out.println("adminUser*********" +
                    "*******##############"+adminUser.toString()); System.out.println("adminUser*********" +
                    "*******##############"+adminUser.toString());
            userRepository.save(adminUser);
            Profile adminProfile = Profile.builder()
                    .name("Admin User")
                    .email(adminEmail)
                    .phoneNumber("000-000-0000")
                    .address(null)
                    .userId(adminUser.getUsername())
                    .build();

            profileRepository.save(adminProfile);

            System.out.println("✅ Default admin user created: " + adminEmail + " / password: admin");
        } else {
            System.out.println("Admin user already exists. Skipping creation.");
        }
    }
}
