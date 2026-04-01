package com.survey_engine.survey.controller.whatsapp;

import com.survey_engine.survey.dto.whatsapp.WhatsAppRequest;
import com.survey_engine.survey.service.whatsapp.WhatsAppSendingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint for sending WhatsApp messages to survey participants.
 */
@RestController
@RequestMapping("/api/v1/whatsapp")
@RequiredArgsConstructor
public class WhatsAppController {

    private final WhatsAppSendingService whatsAppSendingService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(@Valid @RequestBody WhatsAppRequest request) {
        whatsAppSendingService.sendTextMessage(request.to(), request.message());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("status", "QUEUED"));
    }
}
