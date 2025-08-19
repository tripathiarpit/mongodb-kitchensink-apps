// src/main/java/com/example/yourapp/config/ResourceConfig.java

package com.example.yourapp.config;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed; // For unique key
import org.springframework.data.mongodb.core.mapping.Document; // MongoDB document annotation

/**
 * Represents a generic configuration entry in the MongoDB database.
 * The 'configKey' uniquely identifies the configuration (e.g., "appSettings", "emailServiceConfig").
 * The 'configValue' stores the actual configuration data, typically as a JSON string.
 */
@Document(collection = "resource_configs") // Maps this class to a MongoDB collection named 'resource_configs'
public class ResourceConfig {

    @Id // Specifies the primary key of the document (MongoDB's _id field)
    private String id; // Using String for MongoDB's default ObjectId

    @Indexed(unique = true) // Creates a unique index on this field for efficient lookup
    private String configKey; // Unique key for this configuration

    private String configValue; // Stores the configuration as a JSON string

    // Default constructor is required by Spring Data MongoDB
    public ResourceConfig() {
    }

    // Constructor for easy object creation
    public ResourceConfig(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    @Override
    public String toString() {
        return "ResourceConfig{" +
                "id='" + id + '\'' +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                '}';
    }
}
