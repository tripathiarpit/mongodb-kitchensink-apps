package com.mongodb.kitchensink.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson library for JSON processing
import com.mongodb.kitchensink.repository.ResourceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class ResourceConfigService {

    private final ResourceConfigRepository repository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ResourceConfigService(ResourceConfigRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public <T> T saveConfig(String configKey, T configObject) throws JsonProcessingException {
        String jsonValue = objectMapper.writeValueAsString(configObject);

        com.example.yourapp.config.ResourceConfig existingConfig = repository.findByConfigKey(configKey);
        if (existingConfig == null) {
            existingConfig = new com.example.yourapp.config.ResourceConfig();
            existingConfig.setConfigKey(configKey);
        }
        existingConfig.setConfigValue(jsonValue);
        repository.save(existingConfig);
        return configObject;
    }


    public <T> Optional<T> getConfig(String configKey, Class<T> configType) {
        com.example.yourapp.config.ResourceConfig resourceConfig = repository.findByConfigKey(configKey);
        if (resourceConfig != null) {
            try {
                T configObject = objectMapper.readValue(resourceConfig.getConfigValue(), configType);
                return Optional.of(configObject);
            } catch (JsonProcessingException e) {
                System.err.println("Error deserializing config '" + configKey + "': " + e.getMessage());
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    public void deleteConfig(String configKey) {
        com.example.yourapp.config.ResourceConfig resourceConfig = repository.findByConfigKey(configKey);
        if (resourceConfig != null) {
            repository.delete(resourceConfig);
        }
    }
}
