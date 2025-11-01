package com.survey_engine.billing.models;

import com.survey_engine.billing.models.enums.PlanInterval;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a subscription plan template.
 * This entity stores the details of the various subscription plans offered,
 * such as "Basic", "Pro", etc. This is a global entity and not tied to a specific tenant.
 */
@Entity
@Table(name = "billing_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    /**
     * The unique identifier for the plan.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the plan (e.g., "Basic", "Pro").
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * The code for the corresponding plan in the Paystack payment gateway.
     * This is used to identify the plan when creating subscriptions with Paystack.
     */
    /**
     * The code for the corresponding plan in the Paystack payment gateway.
     * This is used to identify the plan when creating subscriptions with Paystack.
     */
    @Column(nullable = false, unique = true)
    private String paystackPlanCode;

    /**
     * The price of the plan.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * The billing interval for the plan (e.g., MONTHLY, YEARLY).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanInterval billingInterval;

    /**
     * A description of the features included in the plan.
     * This can be a simple string or a JSON object with more structured data.
     */
    @Column(columnDefinition = "TEXT")
    private String features;

    /**
     * The timestamp when the entity was created.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * The timestamp when the entity was last updated.
     */
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