package com.mongodb.kitchensink.repository;

import com.mongodb.kitchensink.exception.UserNotFoundException;
import com.mongodb.kitchensink.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email) throws UserNotFoundException;
    boolean existsByUsername(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}
