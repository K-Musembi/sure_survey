package com.survey_engine.survey.service;

import com.survey_engine.survey.models.SmsSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Service to manage the lifecycle of an SMS survey response session in Redis.
 * This service abstracts the Redis operations for creating, retrieving, and deleting session state.
 */
@Service
public class SmsResponseSession {

    private static final String SESSION_KEY_PREFIX = "sms:session:";
    private static final Duration SESSION_TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Constructor for SmsResponseSession.
     * @param redisTemplate The configured RedisTemplate for data access.
     */
    @Autowired
    public SmsResponseSession(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Generates the Redis key for a given session ID.
     * @param sessionId The participant's phone number.
     * @return The namespaced Redis key.
     */
    private String getKey(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    /**
     * Saves or updates a participant's survey session in Redis with a configured Time-To-Live (TTL).
     * @param session The session object to save.
     */
    public void saveSession(SmsSession session) {
        redisTemplate.opsForValue().set(getKey(session.sessionId()), session, SESSION_TTL);
    }

    /**
     * Retrieves a participant's survey session from Redis.
     * @param sessionId The participant's phone number.
     * @return An Optional containing the SmsSession if found, otherwise an empty Optional.
     */
    public Optional<SmsSession> getSession(String sessionId) {
        SmsSession session = (SmsSession) redisTemplate.opsForValue().get(getKey(sessionId));
        return Optional.ofNullable(session);
    }

    /**
     * Deletes a participant's survey session from Redis, typically after completion or timeout.
     * @param sessionId The participant's phone number.
     */
    public void deleteSession(String sessionId) {
        redisTemplate.delete(getKey(sessionId));
    }
}
