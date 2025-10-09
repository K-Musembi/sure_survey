package com.payments.payments.service;

import com.payments.payments.dto.PaymentEventRequest;
import com.payments.payments.dto.paystack.PaystackRequest;
import com.payments.payments.dto.paystack.PaystackResponse;
import com.payments.payments.dto.paystack.PaystackVerifyResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Service class to handle all direct communication with the PayStack API.
 * This class acts as a client for the external PayStack service.
 */
@Service
@AllArgsConstructor
public class PaystackService {

    // Note: PaystackWebClientConfig.java contains a @Bean that is
    // automatically injected by Spring
    private final WebClient paystackClient;

    /**
     * Initializes a payment transaction with PayStack.
     *
     * @param paymentRequest The payment details from our system.
     * @param userEmail The user's email address.
     * @param reference The unique server-generated reference for this transaction.
     * @return A Mono containing the response from PayStack, including the authorization URL.
     */
    public Mono<PaystackResponse> initializePayment(PaymentEventRequest paymentRequest, String userEmail, String reference) {
        // PayStack expects the amount in the smallest currency unit (e.g., cents for KES, cents for USD).
        long amountInSmallestUnit = paymentRequest.amount().multiply(new BigDecimal(100)).longValue();

        PaystackRequest paystackRequest = new PaystackRequest(
                userEmail,
                String.valueOf(amountInSmallestUnit),
                paymentRequest.currency(),
                reference // Use the server-generated reference
        );

        return paystackClient.post()
                .uri("/transaction/initialize")
                .bodyValue(paystackRequest)
                .retrieve()
                .bodyToMono(PaystackResponse.class);
    }

    /**
     * Verifies the status of a transaction with PayStack.
     *
     * @param reference The unique reference ID of the transaction.
     * @return A Mono containing the full transaction details from PayStack.
     */
    public Mono<PaystackVerifyResponse> verifyPayment(String reference) {
        return paystackClient.get()
                .uri("/transaction/verify/{reference}", reference)
                .retrieve()
                .bodyToMono(PaystackVerifyResponse.class);
    }
}