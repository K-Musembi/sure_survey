package com.survey.survey.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configures the application's connection to a Redis server.
 * This is primarily used for managing ephemeral user session state for SMS surveys.
 */
@Configuration
public class RedisConfig {

    /**
     * Creates and configures the RedisTemplate bean for interacting with Redis.
     * The template is configured to use String keys and to serialize/deserialize
     * Java objects to/from JSON for storage in Redis.
     *
     * @param connectionFactory The autoconfigured Redis connection factory.
     * @return A configured RedisTemplate instance.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
