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
 * Represents a prepaid wallet for a tenant.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tenant_wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantWallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private Long tenantId;

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