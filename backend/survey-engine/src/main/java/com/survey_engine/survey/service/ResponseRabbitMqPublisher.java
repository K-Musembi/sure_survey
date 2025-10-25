package com.survey_engine.survey.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.survey.config.rabbitmq.RabbitMqConfig;
import com.survey_engine.survey.dto.ResponseSubmissionPayload;
import com.survey_engine.survey.models.FailedResponseSubmission;
import com.survey_engine.survey.repository.FailedResponseSubmissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * Handles the asynchronous publishing of survey response submissions to RabbitMQ.
 * This class implements a resilient publisher that retries on failure and uses a
 * dead-lettering strategy by saving failed messages to a database table for later inspection or reprocessing.
 */
@Service
@Slf4j
public class ResponseRabbitMqPublisher implements InitializingBean {

    private final RabbitTemplate rabbitTemplate;
    private final FailedResponseSubmissionRepository failedResponseSubmissionRepository;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the publisher with necessary dependencies.
     *
     * @param rabbitTemplate The Spring AMQP template for interacting with RabbitMQ.
     * @param failedResponseSubmissionRepository Repository for persisting submissions that failed to publish.
     * @param objectMapper Jackson mapper for serializing payloads to JSON.
     */
    public ResponseRabbitMqPublisher(@Qualifier("surveyRabbitTemplate") RabbitTemplate rabbitTemplate,
                                     FailedResponseSubmissionRepository failedResponseSubmissionRepository,
                                     ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.failedResponseSubmissionRepository = failedResponseSubmissionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Configures a callback on the RabbitTemplate to handle publisher confirms.
     * This logs an error if the message is not acknowledged by the RabbitMQ broker.
     */
    @Override
    public void afterPropertiesSet() {
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("Message not confirmed by RabbitMQ. Cause: {}", cause);
                // The @Recover method will handle saving the message to the DB
            }
        });
    }

    /**
     * Publishes a survey response payload to the designated RabbitMQ exchange.
     * This method is annotated with {@link Retryable}, which will automatically re-attempt
     * publishing up to 3 times with a 2-second backoff period in case of an {@link AmqpException}.
     *
     * @param payload The survey response data to be published.
     */
    @Retryable(retryFor = AmqpException.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void publishResponse(ResponseSubmissionPayload payload) {
        log.info("Publishing survey response for surveyId: {} to RabbitMQ.", payload.surveyId());
        rabbitTemplate.convertAndSend(RabbitMqConfig.SURVEY_EXCHANGE, RabbitMqConfig.RESPONSE_ROUTING_KEY, payload);
    }

    /**
     * The recovery method for the {@link #publishResponse(ResponseSubmissionPayload)} method.
     * If publishing fails after all retry attempts, this method is invoked. It serializes the
     * payload to a string and persists it to the `failed_response_submission` table to prevent data loss.
     *
     * @param e The {@link AmqpException} that caused the final failure.
     * @param payload The original payload that failed to be published.
     */
    @Recover
    public void recover(AmqpException e, ResponseSubmissionPayload payload) {
        log.error("Failed to publish survey response for surveyId: {} after multiple retries. Saving to database.", payload.surveyId(), e);
        try {
            String payloadAsString = objectMapper.writeValueAsString(payload);
            FailedResponseSubmission failedSubmission = new FailedResponseSubmission();
            failedSubmission.setPayload(payloadAsString);
            failedSubmission.setErrorMessage(e.getMessage());
            failedResponseSubmissionRepository.save(failedSubmission);
        } catch (JsonProcessingException jsonException) {
            log.error("Could not serialize failed payload for surveyId: {}. The submission data is lost.", payload.surveyId(), jsonException);
        }
    }
}
