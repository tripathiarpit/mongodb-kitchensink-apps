package com.mongodb.kitchensink.constants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RedisValue<T> {

    @JsonProperty("value")
    private T value;

    @JsonProperty("expired")
    private boolean expired;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("ttl")
    private Long ttl; // Time to live in seconds

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