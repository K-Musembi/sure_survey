package com.survey_engine.payments.dto.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaystackRequest(
        String email,
        String amount,
        String currency,
        String reference,
        @JsonProperty("callback_url")
        String callbackUrl
) {}
