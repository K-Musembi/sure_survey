package com.survey.survey.sse;

import com.survey.survey.response.dto.ResponseResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Manages the lifecycle of a reactive stream for Server-Sent Events (SSE) using Project Reactor.
 * This service uses a Sink to create a hot stream that can multicast events
 * to multiple subscribers concurrently and handle backpressure.
 */
@Service
public class SseService {

    /**
     * A thread-safe, multicast sink that buffers events for subscribers if they can't keep up.
     * This acts as the central publisher for all new survey response events.
     */
    private final Sinks.Many<ResponseResponse> sink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * Publishes a new survey response to all active SSE subscribers.
     * This method is called by the RabbitMQ listener when a new response is processed.
     * @param response The response DTO to be sent.
     */
    public void publishResponse(ResponseResponse response) {
        sink.tryEmitNext(response);
    }

    /**
     * Allows a new client to subscribe to the stream of survey responses.
     * This method is called by the SseController to establish a new client connection.
     * @return A Flux representing the stream of events.
     */
    public Flux<ResponseResponse> getResponseStream() {
        return sink.asFlux();
    }
}

