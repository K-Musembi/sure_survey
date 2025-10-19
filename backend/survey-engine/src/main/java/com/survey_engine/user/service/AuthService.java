package com.survey_engine.user.service;

import com.survey_engine.user.config.security.JwtService;
import com.survey_engine.user.dto.AuthResponse;
import com.survey_engine.user.dto.LoginRequest;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Registers new user and generates token
     * @param request AuthRequest DTO
     * @return AuthResponse DTO
     */
    @Transactional
    public UserResponse registerUser(SignUpRequest request) {

        Tenant tenant = resolveTenant(request.tenantId());
        if (userRepository.findByEmailAndTenantId(request.email(), tenant.getId()).isPresent()) {
            throw new DataIntegrityViolationException("Email already exists for this tenant");
        }

        String userRole = request.role();
        if (request.tenantId() == null) { // Individual sign-up
            if (userRole == null) {
                userRole = "REGULAR";
            }
        } else { // Enterprise sign-up. If this is the first user for this tenant, make them ADMIN
            if (userRepository.findByTenantId(tenant.getId()).isEmpty()) {
                userRole = "ADMIN";
            } else if (userRole == null) {
                userRole = "REGULAR";
            }
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(userRole);
        user.setTenantId(tenant.getId());

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getTenantId()
        );
    }

    /**
     * Authenticates a user and generates a JWT token.
     * @param request LoginRequest DTO
     * @return AuthResponse DTO containing the JWT token.
     */
    public AuthResponse loginUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        String token = jwtService.generateToken(authentication);
        return new AuthResponse(token);
    }

    /**
     * Find existing tenant or create default tenant
     * @param tenantIdFromRequest - tenant id from request object
     * @return - new or existing tenant
     */
    private Tenant resolveTenant(Long tenantIdFromRequest) {
        if (tenantIdFromRequest == null) {
            return tenantRepository.findBySlug("www")
                    .orElseGet(() -> {
                        Tenant newDefaultTenant = new Tenant();
                        newDefaultTenant.setName("Default Tenant");
                        newDefaultTenant.setSlug("www");
                        newDefaultTenant.setStatus("ACTIVE");
                        newDefaultTenant.setPlan("FREE");
                        return tenantRepository.save(newDefaultTenant);
                    });
        } else {
            // Enterprise sign-up: find existing tenant
            return tenantRepository.findById(tenantIdFromRequest)
                    .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
        }
    }
}