package com.survey_engine.common.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a business transaction (e.g. M-Pesa payment) is received.
 *
 * @param transactionId Internal ID of the transaction record.
 * @param surveyId The ID of the survey configured for this integration.
 * @param msisdn The phone number of the customer.
 * @param firstName Customer's first name.
 * @param lastName Customer's last name.
 * @param amount The transaction amount.
 * @param transactionTime When the transaction occurred.
 */
public record BusinessTransactionEvent(
        java.util.UUID transactionId,
        Long surveyId,
        String msisdn,
        String firstName,
        String lastName,
        BigDecimal amount,
        LocalDateTime transactionTime
) {
}
