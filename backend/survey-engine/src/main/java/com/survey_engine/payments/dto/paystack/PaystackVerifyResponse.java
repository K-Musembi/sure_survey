package com.survey_engine.payments.dto.paystack;

public record PaystackVerifyResponse(
        boolean status,
        String message,
        PaystackVerifyData data
) {}
