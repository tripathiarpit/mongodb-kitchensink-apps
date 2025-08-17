package com.mongodb.kitchensink.config;

import com.github.javafaker.Faker;
import com.mongodb.kitchensink.model.Address;
import com.mongodb.kitchensink.model.Profile;
import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.repository.ProfileRepository;
import com.mongodb.kitchensink.repository.UserRepository;
import com.mongodb.kitchensink.service.UsernameGeneratorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final Faker faker = new Faker();
    private final PasswordEncoder passwordEncoder;
    private  final UsernameGeneratorService usernameGeneratorService;
    public DataLoader(UserRepository userRepository, ProfileRepository profileRepository, PasswordEncoder passwordEncoder, UsernameGeneratorService usernameGeneratorService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.usernameGeneratorService = usernameGeneratorService;
    }

    @Override
    public void run(String... args) {

        String firstUserEmail = "user1@example.com";
        if (userRepository.findByEmail(firstUserEmail).isPresent()) {
            System.out.println("✅ Dummy data already exists, skipping population.");
            return;
        }

        List<User> users = new ArrayList<>();
        List<Profile> profiles = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            String email = "user" + i + "@example.com";
            User user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode("Welcome@123"+i))
                    .roles(List.of("USER"))
                    .username(usernameGeneratorService.generateUniqueUsername(email))
                    .active(true)
                    .createdAt(Instant.now())
                    .isFirstLogin(true)
                    .build();

            users.add(user);
        }

        userRepository.saveAll(users);

        for (User user : users) {
            Address address = new Address();
            address.setStreet(faker.address().streetAddress());
            address.setCity(faker.address().city());
            address.setState(faker.address().state());
            address.setCountry(faker.address().country());
            address.setPincode(faker.address().zipCode());

            Profile profile = Profile.builder()
                    .firstName(faker.name().firstName())
                    .lastName(faker.name().lastName())
                    .email(user.getEmail())
                    .phoneNumber(faker.phoneNumber().cellPhone())
                    .address(address)
                    .username(user.getUsername())
                    .build();

            profiles.add(profile);
        }
        profileRepository.saveAll(profiles);
        System.out.println("✅ Successfully created 100 dummy Users and Profiles");
    }
}
