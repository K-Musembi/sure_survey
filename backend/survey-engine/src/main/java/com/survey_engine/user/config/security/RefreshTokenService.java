package com.survey_engine.user.config.security;

import com.survey_engine.common.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Manages opaque refresh tokens stored in Redis.
 * Key schema: rt:{token} → {userId}
 * TTL: configured via jwt.refresh-expiration-days (default 7 days)
 * On each use the token is rotated (old deleted, new issued) to limit
 * the blast radius of a stolen refresh token.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-expiration-days:7}")
    private long refreshExpirationDays;

    private static final String PREFIX = "rt:";

    /**
     * Create a refresh token for the given user and store it in Redis.
     */
    public String createRefreshToken(String userId) {
        String token = UUID.randomUUID().toString();
        String key = PREFIX + token;
        redisTemplate.opsForValue().set(key, userId, Duration.ofDays(refreshExpirationDays));
        log.debug("Refresh token created for user {}", userId);
        return token;
    }

    /**
     * Validate the token and return the associated userId.
     * Throws BusinessRuleException if the token is unknown or expired.
     */
    public String validateAndGetUserId(String token) {
        String key = PREFIX + token;
        String userId = redisTemplate.opsForValue().get(key);
        if (userId == null) {
            throw new BusinessRuleException("AUTH_REFRESH_TOKEN_INVALID", "Refresh token is invalid or has expired.");
        }
        return userId;
    }

    /**
     * Rotate: delete old token, create and return a new one for the same user.
     */
    public String rotate(String oldToken, String userId) {
        invalidate(oldToken);
        return createRefreshToken(userId);
    }

    /**
     * Invalidate (delete) a refresh token — called on logout.
     */
    public void invalidate(String token) {
        redisTemplate.delete(PREFIX + token);
    }
}
