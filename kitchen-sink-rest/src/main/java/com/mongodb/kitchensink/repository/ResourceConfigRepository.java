package com.mongodb.kitchensink.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceConfigRepository extends MongoRepository<com.example.yourapp.config.ResourceConfig, String> {
    com.example.yourapp.config.ResourceConfig findByConfigKey(String configKey);
}
