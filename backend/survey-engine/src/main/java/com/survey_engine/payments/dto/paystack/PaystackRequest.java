package com.payments.payments.dto.paystack;

public record PaystackRequest(
        String email,
        String amount,
        String currency,
        String reference
) {}
