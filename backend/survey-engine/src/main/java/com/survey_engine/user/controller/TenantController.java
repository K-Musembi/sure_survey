package com.survey_engine.user.controller;

import com.survey_engine.user.dto.TenantRequest;
import com.survey_engine.user.dto.TenantResponse;
import com.survey_engine.user.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing tenant-related operations.
 */
@RestController
@Validated
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    /**
     * Creates a new tenant.
     *
     * @param tenantRequest The request body containing tenant details.
     * @return A {@link ResponseEntity} containing the created tenant's response and HTTP status 201.
     */
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody TenantRequest tenantRequest) {
        TenantResponse responseObject = tenantService.createTenant(tenantRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseObject);
    }

    /**
     * Retrieves a tenant by its ID.
     *
     * @param id The ID of the tenant to retrieve.
     * @return A {@link ResponseEntity} containing the tenant's response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable Long id) {
        TenantResponse responseObject = tenantService.findTenantById(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Retrieves a tenant by its slug.
     *
     * @param slug The slug of the tenant to retrieve.
     * @return A {@link ResponseEntity} containing the tenant's response.
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<TenantResponse> getTenantBySlug(@PathVariable String slug) {
        TenantResponse responseObject = tenantService.findTenantBySlug(slug);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Retrieves all tenants.
     *
     * @return A {@link ResponseEntity} containing a list of all tenants.
     */
    @GetMapping
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        List<TenantResponse> responseObject = tenantService.findAllTenants();
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Updates an existing tenant.
     *
     * @param id The ID of the tenant to update.
     * @param tenantRequest The request body containing updated tenant details.
     * @return A {@link ResponseEntity} containing the updated tenant's response.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody TenantRequest tenantRequest) {
        TenantResponse responseObject = tenantService.updateTenant(id, tenantRequest);
        return ResponseEntity.status(HttpStatus.OK).body(responseObject);
    }

    /**
     * Deletes a tenant by its ID.
     *
     * @param id The ID of the tenant to delete.
     * @return A {@link ResponseEntity} with no content and HTTP status 204.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}