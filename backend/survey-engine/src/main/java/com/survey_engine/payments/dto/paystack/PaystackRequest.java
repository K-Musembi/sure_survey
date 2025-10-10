package com.survey_engine.payments.dto.paystack;

public record PaystackRequest(
        String email,
        String amount,
        String currency,
        String reference
) {}
