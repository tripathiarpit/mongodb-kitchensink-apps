package com.mongodb.kitchensink.repository;

import com.mongodb.kitchensink.model.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends MongoRepository<Profile, String> {

    Optional<Profile> findByUserId(String userId);
    // Paginated city search
    Page<Profile> findByAddress_CityIgnoreCase(String city, Pageable pageable);

    Page<Profile> findByEmailIgnoreCase(String email, Pageable pageable);

}