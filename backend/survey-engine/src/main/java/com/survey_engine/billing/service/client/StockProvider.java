package com.survey_engine.billing.service.client;

import com.survey_engine.billing.models.enums.SystemWalletType;
import java.math.BigDecimal;

/**
 * Interface for external stock providers (e.g., Safaricom, CredoFaster).
 */
public interface StockProvider {
    /**
     * Purchases stock from the external provider.
     * @param type The type of asset to purchase.
     * @param amount The amount to purchase.
     * @return true if the purchase was successful, false otherwise.
     */
    boolean purchaseStock(SystemWalletType type, BigDecimal amount);
}
