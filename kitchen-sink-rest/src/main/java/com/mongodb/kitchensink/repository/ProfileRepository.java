package com.mongodb.kitchensink.repository;

import com.mongodb.kitchensink.model.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProfileRepository extends MongoRepository<Profile, String> {

    Optional<Profile> findByUsername(String username);
    Optional<Profile> findByEmail(String emailId);
    Page<Profile> findByAddress_CountryContainingIgnoreCase(String country, Pageable pageable);
    Page<Profile> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);
    Page<Profile> findByAddress_CityContainingIgnoreCase(String city, Pageable pageable);
}