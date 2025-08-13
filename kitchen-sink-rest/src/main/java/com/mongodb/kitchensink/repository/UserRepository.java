package com.mongodb.kitchensink.repository;

import com.mongodb.kitchensink.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
}
