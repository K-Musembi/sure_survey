package com.survey_engine.billing.models;

import com.survey_engine.billing.models.enums.PaymentGatewayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Maps an internal business {@link Plan} to a specific payment gateway implementation.
 */
@Entity
@Table(name = "billing_plan_gateways")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanGatewayMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway_type", nullable = false)
    private PaymentGatewayType gatewayType;

    /**
     * The code used by the gateway to identify this plan (e.g., Paystack Plan Code).
     */
    @Column(name = "gateway_plan_code", nullable = false)
    private String gatewayPlanCode;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
