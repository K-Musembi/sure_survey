package com.payments.payments.dto.paystack;

public record PaystackVerifyResponse(
        boolean status,
        String message,
        PaystackVerifyData data
) {}
