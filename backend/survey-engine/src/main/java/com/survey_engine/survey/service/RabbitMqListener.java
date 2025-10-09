package com.survey_engine.survey.service;

import com.survey_engine.survey.config.rabbitmq.RabbitMQConfig;
import com.survey_engine.survey.dto.ResponseResponse;
import com.survey_engine.survey.dto.ResponseSubmissionPayload;
import com.survey.survey.sse.SseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Listens for survey submission messages from RabbitMQ, delegates processing,
 * and publishes the result for real-time client notifications.
 */
@Service
public class RabbitMqListener {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqListener.class);
    private final ResponseService responseService;
    private final SseService sseService;

    /**
     * Constructor for RabbitMqListener.
     * @param responseService An instance of ResponseService to process submissions.
     * @param sseService An instance of SseService to publish events.
     */
    @Autowired
    public RabbitMqListener(ResponseService responseService, SseService sseService) {
        this.responseService = responseService;
        this.sseService = sseService;
    }

    /**
     * Listens to the response queue, triggers processing, and notifies clients via SSE.
     * If processing fails, the message is sent to the Dead-Letter Queue.
     *
     * @param payload The survey submission data consumed from the RabbitMQ queue.
     */
    @RabbitListener(queues = RabbitMQConfig.RESPONSE_QUEUE)
    public void processResponseAndNotify(ResponseSubmissionPayload payload) {
        logger.info("Received response submission for survey {}", payload.surveyId());
        try {
            ResponseResponse responseDto = responseService.handleResponseSubmission(payload);
            logger.info("Successfully processed and saved response with ID {}", responseDto.id());

            sseService.publishResponse(responseDto);

        } catch (Exception e) {
            logger.error("Error processing response submission for survey {}: {}", payload.surveyId(), e.getMessage());
            // Re-throwing the exception ensures the message is sent to the DLQ
            throw new RuntimeException("Failed to process response submission", e);
        }
    }
}
