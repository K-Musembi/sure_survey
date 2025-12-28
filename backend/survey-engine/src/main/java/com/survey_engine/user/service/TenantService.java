package com.survey_engine.user.service;

import com.survey_engine.user.dto.TenantRequest;
import com.survey_engine.user.dto.TenantResponse;
import com.survey_engine.user.models.Tenant;
import com.survey_engine.user.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing Tenant entities.
 * Defines business logic for creating, retrieving, updating, and deleting tenants.
 */
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private static final double SIMILARITY_THRESHOLD = 0.8;

    /**
     * Finds tenants with names similar to the given name.
     *
     * @param name The name to compare against.
     * @return A list of {@link TenantResponse} objects with similar names.
     */
    @Transactional
    public List<TenantResponse> findSimilarTenants(String name) {
        List<Tenant> allTenants = tenantRepository.findAll();
        JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();

        return allTenants.stream()
                .filter(tenant -> jaroWinklerDistance.apply(
                        name.toLowerCase(), tenant.getName().toLowerCase()) > SIMILARITY_THRESHOLD)
                .map(this::mapToTenantResponse)
                .collect(Collectors.toList());
    }

    /**
     * Finds an existing tenant by name or creates a new one if it doesn't exist.
     *
     * @param organizationName The name of the organization.
     * @return The found or created {@link Tenant} object.
     */
    @Transactional
    public Tenant findOrCreateTenant(String organizationName) {
        return tenantRepository.findByName(organizationName)
                .orElseGet(() -> {
                    TenantRequest tenantRequest = new TenantRequest(organizationName, organizationName.toLowerCase().replaceAll("\\s+", "-"));
                    return createTenant(tenantRequest);
                });
    }

    /**
     * Creates a new tenant and maps it to a response DTO.
     *
     * @param tenantRequest The DTO containing the details for the new tenant.
     * @return A DTO representing the newly created tenant.
     */
    @Transactional
    public TenantResponse createTenantAndMapToResponse(TenantRequest tenantRequest) {
        Tenant tenant = createTenant(tenantRequest);
        return mapToTenantResponse(tenant);
    }

    /**
     * Creates a new tenant.
     *
     * @param tenantRequest The DTO containing the details for the new tenant.
     * @return The created {@link Tenant} object.
     * @throws DataIntegrityViolationException if a tenant with the given name already exists.
     */
    @Transactional
    public Tenant createTenant(TenantRequest tenantRequest) {
        if (tenantRepository.findByName(tenantRequest.name()).isPresent()) {
            throw new DataIntegrityViolationException("Tenant already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setName(tenantRequest.name());
        if (tenantRequest.slug() != null) {
            tenant.setSlug(tenantRequest.slug());
        }
        // Default status for new tenants
        tenant.setStatus("ACTIVE");

        return tenantRepository.save(tenant);
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

    /**
     * Maps a {@link Tenant} entity to a {@link TenantResponse} DTO.
     *
     * @param tenant The {@link Tenant} entity to map.
     * @return The corresponding {@link TenantResponse} DTO.
     */
    private TenantResponse mapToTenantResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug()
        );
    }
}
