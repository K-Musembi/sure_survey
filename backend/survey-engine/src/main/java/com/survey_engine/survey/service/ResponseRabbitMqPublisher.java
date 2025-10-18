package com.survey_engine.survey.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey_engine.survey.config.rabbitmq.RabbitMQConfig;
import com.survey_engine.survey.dto.ResponseSubmissionPayload;
import com.survey_engine.survey.models.FailedResponseSubmission;
import com.survey_engine.survey.repository.FailedResponseSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResponseRabbitMqPublisher implements InitializingBean {

    private final RabbitTemplate rabbitTemplate;
    private final FailedResponseSubmissionRepository failedResponseSubmissionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() {
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("Message not confirmed by RabbitMQ. Cause: {}", cause);
                // The @Recover method will handle saving the message to the DB
            }
        });
    }

    @Retryable(retryFor = AmqpException.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void publishResponse(ResponseSubmissionPayload payload) {
        log.info("Publishing survey response for surveyId: {} to RabbitMQ.", payload.surveyId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.SURVEY_EXCHANGE, RabbitMQConfig.RESPONSE_ROUTING_KEY, payload);
    }

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