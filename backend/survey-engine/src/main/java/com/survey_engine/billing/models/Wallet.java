package com.survey_engine.billing.models;

import com.survey_engine.common.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a prepaid wallet for a tenant or a user.
 * - For Enterprise Tenants: One wallet per tenant (userId is null).
 * - For Individual Users (Main Tenant): One wallet per user (userId is set).
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (currency == null) {
            currency = "KES"; // Default currency
        }
    }
}
