package com.survey_engine.user.service;

import com.survey_engine.user.dto.SignUpRequest;
import com.survey_engine.user.dto.UserResponse;
import com.survey_engine.user.models.Tenant;
import com.survey_engine.user.repository.TenantRepository;
import com.survey_engine.user.repository.UserRepository;
import com.survey_engine.user.models.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

/**
 * Business logic for authentication service
 * Uses AuthenticationManager to authenticate user
 * Uses generateToken() method from JWTService to generate JWT token
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final TenantService tenantService;

    /**
     * Registers new user and generates token
     * @param request AuthRequest DTO
     * @return AuthResponse DTO
     */
    @Transactional
    public UserResponse registerUser(SignUpRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DataIntegrityViolationException("Email already exists");
        }

        Tenant tenant;
        if (request.tenantId() == null) {
            // Individual sign-up: assign to default tenant
            tenant = tenantRepository.findBySlug("www")
                    .orElseGet(() -> {
                        // Create default tenant if it doesn't exist
                        Tenant defaultTenant = new Tenant();
                        defaultTenant.setName("Default Tenant");
                        defaultTenant.setSlug("www");
                        defaultTenant.setStatus("ACTIVE");
                        defaultTenant.setPlan("FREE");
                        return tenantRepository.save(defaultTenant);
                    });
            // Set role to REGULAR for individual users
            if (request.role() == null) {
                request = new SignUpRequest(request.name(), request.email(), request.password(), "REGULAR", tenant.getId());
            }
        } else {
            // Enterprise sign-up: find existing tenant or create new
            tenant = tenantRepository.findById(request.tenantId())
                    .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
            // If this is the first user for this tenant, make them ADMIN
            if (userRepository.findByTenantId(tenant.getId()).isEmpty()) {
                request = new SignUpRequest(request.name(), request.email(), request.password(), "ADMIN", tenant.getId());
            } else if (request.role() == null) {
                request = new SignUpRequest(request.name(), request.email(), request.password(), "REGULAR", tenant.getId());
            }
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setTenantId(tenant.getId()); // Set tenantId from BaseEntity

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getTenantId()
        );
    }
}