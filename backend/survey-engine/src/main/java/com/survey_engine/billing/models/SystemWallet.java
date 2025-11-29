package com.survey_engine.billing.models;

import com.survey_engine.billing.models.enums.SystemWalletType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents the system's inventory of digital assets (Airtime, Data Bundles).
 * This functions as the "System Store" asset ledger.
 */
@Entity
@Table(name = "system_wallet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemWallet {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type", unique = true, nullable = false)
    private SystemWalletType walletType;

    /**
     * The current available balance (e.g., total Airtime value in KES or total MBs of data).
     */
    @Column(name = "current_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance;

    /**
     * Balance currently reserved for pending reward transactions but not yet disbursed.
     */
    @Column(name = "reserved_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal reservedBalance;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}