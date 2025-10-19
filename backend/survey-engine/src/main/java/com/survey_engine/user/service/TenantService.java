package com.survey_engine.user.service;

import com.survey_engine.user.dto.TenantRequest;
import com.survey_engine.user.dto.TenantResponse;
import com.survey_engine.user.models.Tenant;
import com.survey_engine.user.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing Tenant entities.
 * Defines business logic for creating, retrieving, updating, and deleting tenants.
 */
@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    @Autowired
    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    /**
     * Creates a new tenant.
     *
     * @param tenantRequest The DTO containing the details for the new tenant.
     * @return A DTO representing the newly created tenant.
     * @throws DataIntegrityViolationException if a tenant with the given name already exists.
     */
    @Transactional
    public TenantResponse createTenant(TenantRequest tenantRequest) {
        if (tenantRepository.findByName(tenantRequest.name()).isPresent()) {
            throw new DataIntegrityViolationException("Tenant already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setName(tenantRequest.name());
        if (tenantRequest.slug() != null) {
            tenant.setSlug(tenantRequest.slug());
        }
        // Default status and plan for new tenants
        tenant.setStatus("ACTIVE");
        tenant.setPlan("FREE");
        Tenant savedTenant = tenantRepository.save(tenant);
        return mapToTenantResponse(savedTenant);
    }

    /**
     * Finds a tenant by its ID.
     *
     * @param id The ID of the tenant.
     * @return A DTO representing the found tenant.
     * @throws EntityNotFoundException if no tenant is found with the given ID.
     */
    @Transactional
    public TenantResponse findTenantById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
        return mapToTenantResponse(tenant);
    }

    /**
     * Finds a tenant by its name.
     *
     * @param name The name of the tenant.
     * @return A DTO representing the found tenant.
     * @throws EntityNotFoundException if no tenant is found with the given name.
     */
    @Transactional
    public TenantResponse findTenantByName(String name) {
        Tenant tenant =  tenantRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
        return mapToTenantResponse(tenant);
    }
    
    /**
     * Finds a tenant by its slug.
     *
     * @param slug The slug of the tenant.
     * @return A DTO representing the found tenant.
     * @throws EntityNotFoundException if no tenant is found with the given slug.
     */
    @Transactional
    public TenantResponse findTenantBySlug(String slug) {
        Tenant tenant =  tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
        return mapToTenantResponse(tenant);
    }

    /**
     * Retrieves all tenants.
     *
     * @return A list of DTOs representing all tenants.
     */
    @Transactional
    public List<TenantResponse> findAllTenants() {
        List<Tenant> tenants = tenantRepository.findAll();
        return tenants.stream()
                .map(this::mapToTenantResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing tenant.
     *
     * @param id The ID of the tenant to update.
     * @param tenantRequest The DTO containing the updated details for the tenant.
     * @return A DTO representing the updated tenant.
     * @throws EntityNotFoundException if no tenant is found with the given ID.
     */
    @Transactional
    public TenantResponse updateTenant(Long id, TenantRequest tenantRequest) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));

        tenant.setName(tenantRequest.name());
        tenant.setSlug(tenantRequest.slug());
        Tenant savedTenant = tenantRepository.save(tenant);
        return mapToTenantResponse(savedTenant);
    }

    /**
     * Deletes a tenant by its ID.
     *
     * @param id The ID of the tenant to delete.
     * @throws EntityNotFoundException if no tenant is found with the given ID.
     */
    @Transactional
    public void deleteTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
        tenantRepository.delete(tenant);
    }

    private TenantResponse mapToTenantResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug()
        );
    }
}
