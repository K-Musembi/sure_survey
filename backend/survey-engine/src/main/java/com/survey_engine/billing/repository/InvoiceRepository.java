package com.survey_engine.billing.repository;

import com.survey_engine.billing.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Invoice} entity.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    /**
     * Finds all Invoice entities for a given tenant ID.
     *
     * @param tenantId The ID of the tenant.
     * @return A list of invoices for that tenant.
     */
    List<Invoice> findByTenantId(String tenantId);

    /**
     * Finds an Invoice entity by its Paystack invoice ID.
     *
     * @param paystackInvoiceId The Paystack invoice ID.
     * @return An {@link Optional} containing the found Invoice or empty if not found.
     */
    Optional<Invoice> findByPaystackInvoiceId(String paystackInvoiceId);
}