package com.payments.payments.dto;

/**
 * Response Data Transfer Object (DTO) sent after successfully initializing a payment.
 * This contains the information the frontend needs to launch the PayStack payment popup.
 *
 * @param authorizationUrl The URL to redirect the user to for payment.
 * @param accessCode       The access code for the transaction.
 * @param reference        The unique reference for the transaction.
 */
public record PaymentEventResponse(
        String authorizationUrl,
        String accessCode,
        String reference
) {}
