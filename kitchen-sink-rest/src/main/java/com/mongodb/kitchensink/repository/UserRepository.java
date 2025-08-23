package com.mongodb.kitchensink.repository;

import com.mongodb.kitchensink.exception.UserNotFoundException;
import com.mongodb.kitchensink.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email) throws UserNotFoundException;
    boolean existsByUsername(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    long countByActiveTrue();
    long countByIsAccountVerificationPendingTrue();
    long countByIsFirstLoginTrue();
    long countByRoles(String role);
    @Query(value = "{ 'roles': ?0 }", count = true)
    long countByExactRoles(List<String> roles);
    long countByCreatedAtAfter(Instant date);
    @Query(value = "{ 'roles': { $all: [?0, ?1] } }", count = true)
    long countUsersWithRoles(String role1, String role2);
    List<User> findByEmailIn(List<String> emails);
}
