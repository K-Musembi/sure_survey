package com.survey_engine.payments.dto.paystack;

import java.math.BigDecimal;

public record PaystackVerifyData(
        String status,
        String reference,
        BigDecimal amount,
        String channel,
        String currency
) {}
