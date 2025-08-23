package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Service
public class UsernameGeneratorService {

    private final UserRepository userRepository;
    SecureRandom random = new SecureRandom();
    byte bytes[] = new byte[20];

    public UsernameGeneratorService(UserRepository userRepository) {
        random.nextBytes(bytes);
        this.userRepository = userRepository;
    }

    public String generateUniqueUsername(String email) {
        String baseUsername = email.substring(0, email.indexOf("@")).toLowerCase();
        String username = baseUsername;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + randomSuffix(4);
        }

        return username;
    }

    private String randomSuffix(int length) {
        String characters = "abcdef6789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
