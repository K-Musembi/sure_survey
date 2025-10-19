package com.survey_engine.survey.controller.sms;

import com.survey_engine.survey.service.sms.SmsResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to handle inbound SMS messages for survey responses.
 */
@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
public class SmsResponseController {

    private final SmsResponseService smsResponseService;

    /**
     * Handles the incoming SMS from the webhook.
     * @param from The phone number of the sender.
     * @param body The content of the SMS message.
     * @return A plain text response to be sent back to the user.
     */
    @PostMapping(path = "/inbound", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handleSms(@RequestParam("From") String from, @RequestParam("Body") String body) {
        // Sanitize phone number to remove any non-digit characters
        String sanitizedFrom = from.replaceAll("[^\\d]", "");
        String responseMessage = smsResponseService.handleSmsRequest(sanitizedFrom, body);
        return ResponseEntity.ok(responseMessage);
    }
}