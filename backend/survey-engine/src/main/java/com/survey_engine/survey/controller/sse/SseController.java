package com.survey_engine.survey.controller.sse;

import com.survey_engine.survey.dto.ResponseResponse;
import com.survey_engine.survey.service.sse.SseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Controller providing the reactive HTTP endpoint for clients to establish
 * a Server-Sent Events (SSE) connection using Project Reactor's Flux.
 */
@RestController
@RequestMapping("/api/v1/responses")
public class SseController {

    private final SseService sseService;

    /**
     * Constructor for SseController.
     * @param sseService An instance of SseService to manage the reactive stream.
     */
    @Autowired
    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    /**
     * Establishes a reactive SSE connection with a client.
     * Each client receives a stream of events, including new survey responses
     * and periodic keep-alive comments.
     * @return A Flux of ServerSentEvent objects.
     */
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ResponseResponse>> stream() {
        Flux<ServerSentEvent<ResponseResponse>> responseEventFlux = sseService.getResponseStream()
                .map(response -> ServerSentEvent.<ResponseResponse>builder()
                        .event("new-response")
                        .data(response)
                        .build());

        Flux<ServerSentEvent<ResponseResponse>> keepAliveFlux = Flux.interval(Duration.ofSeconds(15))
                .map(i -> ServerSentEvent.<ResponseResponse>builder()
                        .comment("keep-alive")
                        .build());

        // Merge the two streams. The client will receive both data events and keep-alive comments.
        return Flux.merge(responseEventFlux, keepAliveFlux);
    }
}

