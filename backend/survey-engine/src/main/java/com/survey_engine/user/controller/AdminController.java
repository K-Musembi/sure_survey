package com.survey_engine.user.controller;

import com.survey_engine.common.auditing.Auditable;
import com.survey_engine.user.dto.*;
import com.survey_engine.user.service.AdminService;
import com.survey_engine.user.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                loginResponse.user().getTenantId(),
                loginResponse.user().getTenant() != null ? loginResponse.user().getTenant().getName() : "SYSTEM"
        );

        return ResponseEntity.ok(userResponse);
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