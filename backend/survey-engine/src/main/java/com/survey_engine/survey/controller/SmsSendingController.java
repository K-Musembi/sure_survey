package com.survey_engine.survey.controller;

import com.survey_engine.survey.dto.SmsRequest;
import com.survey_engine.survey.service.SmsSendingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
@Validated
public class SmsSendingController {

    private final SmsSendingService smsSendingService;

    /**
     * Endpoint to send an SMS message.
     * This is a generic endpoint that can be used to send any SMS.
     * The caller is responsible for constructing the message content.
     *
     * @param smsRequest The request body containing the recipient and message.
     * @param jwt        The JWT of the authenticated user, for authorization.
     * @return A ResponseEntity indicating the request was accepted.
     */
    @PostMapping("/send")
    public ResponseEntity<Void> sendSms(
            @Valid @RequestBody SmsRequest smsRequest,
            @AuthenticationPrincipal Jwt jwt) {

        // The presence of the @AuthenticationPrincipal annotation ensures this endpoint is protected.
        // We can add more specific role-based authorization here if needed.

        smsSendingService.sendSms(smsRequest.to(), smsRequest.message());

        // Return 202 Accepted as the SMS is sent asynchronously by the provider.
        return ResponseEntity.accepted().build();
    }
}

