package com.survey_engine.payments.dto.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaystackResponse(
        boolean status,
        String message,
        PaystackResponseData data
) {}
