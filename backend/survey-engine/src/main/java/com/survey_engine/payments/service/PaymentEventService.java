package com.survey_engine.payments.service;

import com.survey_engine.payments.dto.PaymentEventDetails;
import com.survey_engine.payments.dto.PaymentEventRequest;
import com.survey_engine.payments.dto.PaymentEventResponse;
import com.survey_engine.payments.dto.paystack.PaystackResponse;
import com.survey_engine.payments.models.PaymentEvent;
import com.survey_engine.payments.models.enums.PaymentGateway;
import com.survey_engine.payments.models.enums.PaymentStatus;
import com.survey_engine.payments.repository.PaymentEventRepository;
import com.survey_engine.user.service.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentEventService {

    private final PaymentEventRepository paymentRepository;
    private final PaystackService paystackService;


    @Transactional
    public Mono<PaymentEventResponse> createPaymentEvent(PaymentEventRequest request, String userId, String userEmail) {
        Long tenantId = TenantContext.getTenantId();
        // Check for idempotency key first
        return Mono.just(request)
                .flatMap(req -> {
                    if (paymentRepository.findByIdempotencyKeyAndTenantId(req.idempotencyKey(), tenantId).isPresent()) {
                        log.warn("Idempotency key conflict: {}", req.idempotencyKey());
                        return Mono.error(new DataIntegrityViolationException("A paymentEvent with this idempotency key already exists."));
                    }
                    return Mono.just(req);
                })
                .flatMap(req -> {
                    String reference = UUID.randomUUID().toString();
                    return paystackService.initializePayment(req, userEmail, reference)
                            .flatMap(paystackResponse -> {
                                if (paystackResponse == null || !paystackResponse.status()) {
                                    log.error("PayStack initialization failed: {}", paystackResponse != null ? paystackResponse.message() : "No response");
                                    return Mono.error(new IllegalStateException("PaymentEvent gateway failed to initialize transaction."));
                                }
                                PaymentEvent paymentEvent = persistPaymentEvent(req, paystackResponse, userId, userEmail, tenantId);
                                log.info("Successfully created and saved pending paymentEvent with reference: {}", paymentEvent.getGatewayTransactionId());

                                return Mono.just(new PaymentEventResponse(
                                        paystackResponse.data().authorizationUrl(),
                                        paystackResponse.data().accessCode(),
                                        paystackResponse.data().reference()
                                ));
                            });
                });
    }

    @Transactional(readOnly = true)
    public PaymentEventDetails findPaymentEventById(UUID id) {
        Long tenantId = TenantContext.getTenantId();
        return paymentRepository.findById(id)
                .filter(pe -> pe.getTenantId().equals(tenantId))
                .map(this::mapToPaymentEventDetails)
                .orElseThrow(() -> new EntityNotFoundException("PaymentEvent with ID " + id + " not found."));
    }

    @Transactional(readOnly = true)
    public List<PaymentEventDetails> findPaymentEventsByUserId(String userId) {
        Long tenantId = TenantContext.getTenantId();
        return paymentRepository.findByUserIdAndTenantId(userId, tenantId).stream()
                .map(this::mapToPaymentEventDetails)
                .collect(Collectors.toList());
    }

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
