package com.survey_engine.billing.service;

import com.survey_engine.billing.dto.paystack.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service for interacting with Paystack's subscription-related APIs.
 * This service handles creating customers, creating subscriptions, and canceling subscriptions
 * directly with Paystack. This implementation uses Project Reactor for non-blocking I/O.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaystackSubscriptionService {

    private final WebClient paystackBillingClient;

    /**
     * Creates a customer in Paystack reactively.
     *
     * @param email The customer's email address.
     * @param firstName The first name of the customer.
     * @param lastName The last name of the customer.
     * @param phone The phone number of the customer.
     * @return A {@link Mono} emitting the Paystack customer code upon successful creation.
     */
    public Mono<String> createCustomer(String email, String firstName, String lastName, String phone) {
        log.info("Creating Paystack customer for email: {}", email);
        PaystackCustomerRequest request = new PaystackCustomerRequest(email, firstName, lastName, phone);

        return paystackBillingClient.post()
                .uri("/customer")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaystackCustomerResponse.class)
                .flatMap(response -> {
                    if (response != null && response.status() && response.data() != null) {
                        log.info("Paystack customer created with code: {}", response.data().customerCode());
                        return Mono.just(response.data().customerCode());
                    } else {
                        String errorMessage = response != null ? response.message() : "No response";
                        log.error("Failed to create Paystack customer: {}", errorMessage);
                        return Mono.error(new RuntimeException("Failed to create Paystack customer. Reason: " + errorMessage));
                    }
                });
    }

    /**
     * Creates a subscription in Paystack reactively.
     *
     * @param customerCode The Paystack customer code.
     * @param planCode The Paystack plan code.
     * @return A {@link Mono} emitting the {@link PaystackSubscriptionData} upon successful creation.
     */
    public Mono<PaystackSubscriptionData> createSubscription(String customerCode, String planCode) {
        log.info("Creating Paystack subscription for customer: {} and plan: {}", customerCode, planCode);
        PaystackSubscriptionRequest request = new PaystackSubscriptionRequest(customerCode, planCode, null);

        return paystackBillingClient.post()
                .uri("/subscription")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaystackSubscriptionResponse.class)
                .flatMap(response -> {
                    if (response != null && response.status() && response.data() != null) {
                        log.info("Paystack subscription created with code: {}", response.data().subscriptionCode());
                        return Mono.just(response.data());
                    } else {
                        String errorMessage = response != null ? response.message() : "No response";
                        log.error("Failed to create Paystack subscription: {}", errorMessage);
                        return Mono.error(new RuntimeException("Failed to create Paystack subscription. Reason: " + errorMessage));
                    }
                });
    }

    /**
     * Cancels a subscription in Paystack reactively.
     *
     * @param subscriptionCode The Paystack subscription code.
     * @param emailToken The email token for confirmation (if required by Paystack).
     * @return A {@link Mono} emitting {@code true} if cancellation was successful.
     */
    public Mono<Boolean> cancelSubscription(String subscriptionCode, String emailToken) {
        log.info("Cancelling Paystack subscription: {}", subscriptionCode);
        PaystackDisableSubscriptionRequest request = new PaystackDisableSubscriptionRequest(subscriptionCode, emailToken);

        return paystackBillingClient.post()
                .uri("/subscription/disable")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaystackDisableSubscriptionResponse.class)
                .flatMap(response -> {
                    if (response != null && response.status()) {
                        log.info("Paystack subscription {} cancelled successfully.", subscriptionCode);
                        return Mono.just(true);
                    } else {
                        String errorMessage = response != null ? response.message() : "No response";
                        log.error("Failed to cancel Paystack subscription {}: {}", subscriptionCode, errorMessage);
                        return Mono.error(new RuntimeException("Failed to cancel Paystack subscription. Reason: " + errorMessage));
                    }
                });
    }
}

