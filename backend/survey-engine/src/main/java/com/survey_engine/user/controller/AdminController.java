package com.survey_engine.user.controller;

import com.survey_engine.user.dto.*;
import com.survey_engine.common.auditing.Auditable;
import com.survey_engine.user.service.AdminService;
import com.survey_engine.user.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling SUPER_ADMIN specific HTTP requests.
 * All endpoints in this controller are protected and require SUPER_ADMIN role.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    /**
     * Endpoint for SUPER_ADMIN login.
     * This endpoint is an exception to the class-level PreAuthorize,
     * as it must be accessible for unauthenticated super admin users to log in.
     *
     * @param request The login request details (email and password).
     * @param response The HTTP servlet response to add the cookie to.
     * @return A ResponseEntity containing the user response.
     */
    @PostMapping("/login")
    @PreAuthorize("permitAll()") // Override class-level security for this specific endpoint
    public ResponseEntity<UserResponse> loginSuperAdmin(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.loginSuperAdmin(request);
        response.addCookie(createCookie(loginResponse.token()));

        UserResponse userResponse = new UserResponse(
                loginResponse.user().getId(),
                loginResponse.user().getName(),
                loginResponse.user().getEmail(),
                loginResponse.user().getDepartment(),
                loginResponse.user().getRegion(),
                loginResponse.user().getBranch(),
                loginResponse.user().getTenantId(),
                loginResponse.user().getTenant() != null ? loginResponse.user().getTenant().getName() : "SYSTEM"
        );

        return ResponseEntity.ok(userResponse);
    }

    /**
     * Endpoint to register a new system administrator.
     * This action is audited.
     *
     * @param request The sign-up request details.
     * @return A ResponseEntity containing the user response.
     */
    @PostMapping("/signup")
    @Auditable(action = "SYSTEM_ADMIN_SIGNUP")
    public ResponseEntity<UserResponse> registerSystemAdmin(@Valid @RequestBody SignUpRequest request) {
        UserResponse responseObject = adminService.registerSystemAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseObject);
    }

    /**
     * Retrieves a list of all tenants with their details, including user count.
     * This action is audited.
     *
     * @return A ResponseEntity containing a list of TenantDetailsResponse.
     */
    @GetMapping("/tenants")
    @Auditable(action = "VIEW_ALL_TENANTS")
    public ResponseEntity<List<TenantDetailsResponse>> getAllTenants() {
        List<TenantDetailsResponse> tenants = adminService.getAllTenantsWithDetails();
        return ResponseEntity.ok(tenants);
    }

    /**
     * Retrieves all surveys for a specific tenant.
     * This action is audited.
     *
     * @param tenantId The ID of the tenant.
     * @return A ResponseEntity containing a list of SurveyResponse.
     */
    @GetMapping("/tenants/{tenantId}/surveys")
    @Auditable(action = "VIEW_TENANT_SURVEYS")
    public ResponseEntity<List<TenantSurveysResponse>> getSurveysForTenant(@PathVariable Long tenantId) {
        List<TenantSurveysResponse> surveys = adminService.getSurveysForTenant(tenantId);
        return ResponseEntity.ok(surveys);
    }

    /**
     * Retrieves all system configuration settings.
     *
     * @return A ResponseEntity containing a list of SystemSettingResponse.
     */
    @GetMapping("/settings")
    public ResponseEntity<List<SystemSettingResponse>> getSystemSettings() {
        return ResponseEntity.ok(adminService.getAllSystemSettings());
    }

    /**
     * Updates system configuration settings.
     * This action is audited.
     *
     * @param requests A list of SystemSettingRequest.
     * @return A ResponseEntity containing the updated settings.
     */
    @PutMapping("/settings")
    @Auditable(action = "UPDATE_SYSTEM_SETTINGS")
    public ResponseEntity<List<SystemSettingResponse>> updateSystemSettings(
            @Valid @RequestBody List<SystemSettingRequest> requests) {
        return ResponseEntity.ok(adminService.updateSystemSettings(requests));
    }

    /**
     * Creates a new subscription plan.
     */
    @PostMapping("/plans")
    @Auditable(action = "CREATE_PLAN")
    public ResponseEntity<Long> createPlan(@Valid @RequestBody PlanCreationRequest request) {
        Long planId = adminService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(planId);
    }

    /**
     * Configures a payment gateway for a plan.
     */
    @PostMapping("/plans/{planId}/gateways")
    @Auditable(action = "CONFIGURE_PLAN_GATEWAY")
    public ResponseEntity<Void> configurePlanGateway(
            @PathVariable Long planId,
            @Valid @RequestBody PlanGatewayConfigurationRequest request) {
        adminService.configurePlanGateway(planId, request.gatewayType(), request.gatewayPlanCode());
        return ResponseEntity.ok().build();
    }

    /**
     * Updates a subscription plan configuration.
     */
    @PutMapping("/plans")
    @Auditable(action = "UPDATE_PLAN")
    public ResponseEntity<Void> updatePlan(@RequestBody Map<String, Object> request) {
        Long planId = ((Number) request.get("planId")).longValue();
        BigDecimal price = request.get("price") != null ? new BigDecimal(request.get("price").toString()) : null;
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> features = (java.util.Map<String, Object>) request.get("features");

        adminService.updatePlan(planId, price, features);
        return ResponseEntity.ok().build();
    }
    
    // NOTE: I need to change the parameter type of updatePlan in AdminController from PlanUpdateRequest to Map.

    /**
     * Restocks the system wallet (inventory) via external provider.
     */
    @PostMapping("/system-wallet/restock")
    @Auditable(action = "RESTOCK_SYSTEM_WALLET")
    public ResponseEntity<Void> restockSystemWallet(
            @RequestParam String type,
            @RequestParam BigDecimal amount) {
        adminService.restockSystemWallet(type, amount);
        return ResponseEntity.ok().build();
    }

    /**
     * Creates a secure, HTTP-only cookie for the JWT token.
     *
     * @param token The JWT token.
     * @return A configured Cookie.
     */
    private Cookie createCookie(String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        // cookie.setSecure(true); // Should be enabled in production (HTTPS)
        cookie.setMaxAge(60 * 60 * 8); // 8 hours
        return cookie;
    }
}