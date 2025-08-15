package com.mongodb.kitchensink.constants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true) // This will ignore any unknown fields
public class RedisValue<T> {

    @JsonProperty("value")
    private T value;

    @JsonProperty("expired")
    private boolean expired;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("ttl")
    private Long ttl; // Time to live in seconds

    // Default constructor for Jackson
    public RedisValue() {}

    public RedisValue(T value) {
        this.value = value;
        this.expired = false;
        this.timestamp = LocalDateTime.now();
    }

    public RedisValue(T value, Long ttl) {
        this.value = value;
        this.expired = false;
        this.timestamp = LocalDateTime.now();
        this.ttl = ttl;
    }

    // Getters and setters
    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
    public void refresh(long ttlSeconds) {
        this.ttl = ttlSeconds;
        this.timestamp = LocalDateTime.now();
    }
    public boolean isExpired() {
        if (expired) {
            return true;
        }

        // Check if TTL has passed
        if (ttl != null && timestamp != null) {
            LocalDateTime expiryTime = timestamp.plusSeconds(ttl);
            return LocalDateTime.now().isAfter(expiryTime);
        }

        return false;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    // Utility method to check if value is valid (not null and not expired)
    public boolean isValid() {
        return value != null && !isExpired();
    }

    @Override
    public String toString() {
        return "RedisValue{" +
                "value=" + value +
                ", expired=" + expired +
                ", timestamp=" + timestamp +
                ", ttl=" + ttl +
                '}';
    }
}