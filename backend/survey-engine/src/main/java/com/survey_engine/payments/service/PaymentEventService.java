package com.survey_engine.payments.service;

import com.survey_engine.payments.dto.PaymentEventDetails;
import com.survey_engine.payments.dto.PaymentEventRequest;
import com.survey_engine.payments.dto.PaymentEventResponse;
import com.survey_engine.payments.dto.paystack.PaystackResponse;
import com.survey_engine.payments.models.PaymentEvent;
import com.survey_engine.payments.models.enums.PaymentGateway;
import com.survey_engine.payments.models.enums.PaymentStatus;
import com.survey_engine.payments.repository.PaymentEventRepository;
import com.survey_engine.user.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentEventService {

    private final PaymentEventRepository paymentRepository;
    private final PaystackService paystackService;
    private final UserApi userApi;

    /**
     * Initiates a new payment event, handling idempotency and interaction with the Paystack gateway.
     * This method orchestrates the steps of validating the request, calling the payment gateway,
     * persisting the payment event, and returning the necessary details for the client to complete the payment.
     *
     * @param request The {@link PaymentEventRequest} containing payment details.
     * @param userId The ID of the user initiating the payment.
     * @param userEmail The email of the user, used for Paystack initialization.
     * @return A {@link Mono} emitting a {@link PaymentEventResponse} with authorization details.
     */
    @Transactional
    public Mono<PaymentEventResponse> createPaymentEvent(PaymentEventRequest request, String userId, String userEmail) {
        Long tenantId = userApi.getTenantId();

        return validateIdempotency(request.idempotencyKey(), tenantId)
                .then(Mono.fromCallable(() -> UUID.randomUUID().toString())) // Generate reference after idempotency check
                .flatMap(reference -> paystackService.initializePayment(request, userEmail, reference))
                .flatMap(this::validatePaystackInitializationResponse)
                .flatMap(paystackResponse -> {
                    PaymentEvent paymentEvent = persistPaymentEvent(request, paystackResponse, userId, userEmail, tenantId);
                    log.info("Successfully created and saved pending paymentEvent with reference: {}", paymentEvent.getGatewayTransactionId());

                    return Mono.just(new PaymentEventResponse(
                            paystackResponse.data().authorizationUrl(),
                            paystackResponse.data().accessCode(),
                            paystackResponse.data().reference()
                    ));
                });
    }

    /**
     * Validates the idempotency key to prevent duplicate payment initiations.
     * This method performs a blocking database call on a dedicated scheduler.
     *
     * @param idempotencyKey The unique client-generated key.
     * @param tenantId The ID of the current tenant.
     * @return A {@link Mono<Void>} that completes if the key is unique, or emits an error if a duplicate is found.
     */
    private Mono<Void> validateIdempotency(String idempotencyKey, Long tenantId) {
        return Mono.fromCallable(() -> paymentRepository.findByIdempotencyKeyAndTenantId(idempotencyKey, tenantId).isPresent())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(isDuplicate -> {
                    if (isDuplicate) {
                        log.warn("Idempotency key conflict: {}", idempotencyKey);
                        return Mono.error(new DataIntegrityViolationException("A paymentEvent with this idempotency key already exists."));
                    }
                    return Mono.empty(); // Signal completion if unique
                });
    }

    /**
     * Validates the response received from the Paystack payment initialization API.
     * Emits an error if the initialization was unsuccessful.
     *
     * @param paystackResponse The {@link PaystackResponse} from the Paystack API.
     * @return A {@link Mono} emitting the validated {@link PaystackResponse} if successful.
     */
    private Mono<PaystackResponse> validatePaystackInitializationResponse(PaystackResponse paystackResponse) {
        if (paystackResponse == null || !paystackResponse.status()) {
            log.error("PayStack initialization failed: {}", paystackResponse != null ? paystackResponse.message() : "No response");
            return Mono.error(new IllegalStateException("PaymentEvent gateway failed to initialize transaction."));
        }
        return Mono.just(paystackResponse);
    }

    /**
     * Finds a payment event by its ID, scoped by the current tenant.
     *
     * @param id The UUID of the payment event.
     * @return A {@link PaymentEventDetails} DTO representing the found payment event.
     * @throws EntityNotFoundException if the payment event is not found for the given ID and tenant.
     */
    @Transactional(readOnly = true)
    public PaymentEventDetails findPaymentEventById(UUID id) {
        Long tenantId = userApi.getTenantId();
        return paymentRepository.findById(id)
                .filter(pe -> pe.getTenantId().equals(tenantId))
                .map(this::mapToPaymentEventDetails)
                .orElseThrow(() -> new EntityNotFoundException("PaymentEvent with ID " + id + " not found."));
    }

    /**
     * Finds all payment events for a given user ID, scoped by the current tenant.
     *
     * @param userId The ID of the user.
     * @return A list of {@link PaymentEventDetails} DTOs for the specified user and tenant.
     */
    @Transactional(readOnly = true)
    public List<PaymentEventDetails> findPaymentEventsByUserId(String userId) {
        Long tenantId = userApi.getTenantId();
        return paymentRepository.findByUserIdAndTenantId(userId, tenantId).stream()
                .map(this::mapToPaymentEventDetails)
                .collect(Collectors.toList());
    }

    /**
     * Persists a new {@link PaymentEvent} entity to the database.
     *
     * @param request The original {@link PaymentEventRequest}.
     * @param paystackResponse The successful {@link PaystackResponse} from initialization.
     * @param userId The ID of the user.
     * @param userEmail The email of the user.
     * @param tenantId The ID of the current tenant.
     * @return The persisted {@link PaymentEvent} entity.
     */
    private PaymentEvent persistPaymentEvent(
            PaymentEventRequest request, PaystackResponse paystackResponse, String userId, String userEmail, Long tenantId) {

        PaymentEvent paymentEvent = new PaymentEvent();
        paymentEvent.setTenantId(tenantId);
        paymentEvent.setUserId(userId);
        paymentEvent.setEmail(userEmail);
        paymentEvent.setSurveyId(request.surveyId());
        paymentEvent.setIdempotencyKey(request.idempotencyKey());
        paymentEvent.setAmount(request.amount());
        paymentEvent.setCurrency(request.currency());
        paymentEvent.setStatus(PaymentStatus.PENDING);
        paymentEvent.setPaymentGateway(PaymentGateway.PAYSTACK);
        paymentEvent.setGatewayTransactionId(paystackResponse.data().reference());

        return paymentRepository.save(paymentEvent);
    }

    /**
     * Maps a {@link PaymentEvent} entity to a {@link PaymentEventDetails} DTO.
     *
     * @param paymentEvent The {@link PaymentEvent} entity to map.
     * @return The corresponding {@link PaymentEventDetails} DTO.
     */
    private PaymentEventDetails mapToPaymentEventDetails(PaymentEvent paymentEvent) {
        return new PaymentEventDetails(
                paymentEvent.getId(),
                paymentEvent.getSurveyId(),
                paymentEvent.getAmount(),
                paymentEvent.getCurrency(),
                paymentEvent.getStatus(),
                paymentEvent.getGatewayTransactionId(),
                paymentEvent.getCreatedAt()
        );
    }
}
