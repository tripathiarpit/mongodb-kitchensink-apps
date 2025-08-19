package com.mongodb.kitchensink.util;

import com.mongodb.kitchensink.constants.RedisValue;
import org.springframework.stereotype.Component;

@Component
public class TimeCalculationUtil {
    private static final long MIN_TTL_SECONDS = 5;
    public long calculateNewRedisTTL(RedisValue<?> existingRedisValue, long newConfigTotalSeconds) {
        // The existingRedisValue's internal timestamp and ttl will be updated by SessionService.
        // The Redis TTL for the key should simply reflect the new configured total duration.
        return Math.max(newConfigTotalSeconds, MIN_TTL_SECONDS);
    }
}