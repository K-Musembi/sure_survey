package com.survey_engine.user.service;

import com.survey_engine.user.config.security.JwtService;
import com.survey_engine.user.dto.LoginRequest;
import com.survey_engine.user.dto.LoginResponse;
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

        String userRole = assignUserRole(request, tenant);

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
    public LoginResponse loginUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(authentication);
        return new LoginResponse(token, user);
    }

    /**
     * Assigns a role to the user based on the sign-up request and tenant context.
     * If it's an individual sign-up (no tenantId provided), defaults to 'REGULAR'.
     * If it's an enterprise sign-up and the first user for that tenant, assigns 'ADMIN'.
     * Otherwise, defaults to 'REGULAR' or uses the role provided in the request.
     *
     * @param request The SignUpRequest containing user details.
     * @param tenant The Tenant entity the user is signing up for.
     * @return The assigned role for the user.
     */
    private String assignUserRole(SignUpRequest request, Tenant tenant) {
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
        return userRole;
    }

    /**
     * Find existing tenant or create default tenant
     * @param tenantIdFromRequest - tenant id from request object
     * @return - new or existing tenant
     */
    private Tenant resolveTenant(Long tenantIdFromRequest) {
        if (tenantIdFromRequest == null) {
            // For individual sign-up, assume 'www' tenant exists and find it.
            return tenantRepository.findBySlug("www")
                    .orElseThrow(() -> new EntityNotFoundException("Default 'www' tenant not found. Please ensure it is initialized."));
        } else {
            // Enterprise sign-up: find existing tenant
            return tenantRepository.findById(tenantIdFromRequest)
                    .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
        }
    }
}