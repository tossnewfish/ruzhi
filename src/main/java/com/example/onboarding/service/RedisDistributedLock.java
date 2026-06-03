package com.example.onboarding.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RedisDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

    private final StringRedisTemplate redisTemplate;

    public RedisDistributedLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<LockToken> tryLock(String key, Duration ttl) {
        String token = UUID.randomUUID().toString();
        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, token, ttl);
            return Boolean.TRUE.equals(success) ? Optional.of(new LockToken(key, token)) : Optional.empty();
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Redis is unavailable, cannot acquire distributed lock.", ex);
        }
    }

    public void unlock(LockToken lockToken) {
        String script = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
                """;
        try {
            redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), List.of(lockToken.key()), lockToken.value());
        } catch (DataAccessException ex) {
            log.warn("Failed to release redis lock {}: {}", lockToken.key(), ex.getMessage());
        }
    }

    public record LockToken(String key, String value) {
    }
}
