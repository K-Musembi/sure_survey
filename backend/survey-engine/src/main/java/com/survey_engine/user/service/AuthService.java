package com.survey_engine.user.service;

import com.survey_engine.common.events.UserRegisteredEvent;
import com.survey_engine.user.config.security.JwtService;
import com.survey_engine.user.dto.*;
import com.survey_engine.user.models.Tenant;
import com.survey_engine.user.repository.TenantRepository;
import com.survey_engine.user.repository.UserRepository;
import com.survey_engine.user.models.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    /**
     * Registers new user and generates token
     * @param request AuthRequest DTO
     * @return AuthResponse DTO
     */
    @Transactional
    public UserResponse registerUser(SignUpRequest request) {

        Tenant tenant = resolveTenant(request.organization());
        if (userRepository.findByEmailAndTenantId(request.email(), tenant.getId()).isPresent()) {
            throw new DataIntegrityViolationException("Email already exists for this tenant");
        }

        String userRole = assignUserRole(request, tenant);

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(userRole);
        user.setDepartment(request.department());
        user.setRegion(request.region());
        user.setBranch(request.branch());
        user.setTenantId(tenant.getId());

        User savedUser = userRepository.save(user);

        // Publish event for billing module to handle subscription creation
        // The listener will determine if a new subscription is needed based on tenant type and existing subs
        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser.getId(), tenant.getId()));

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getDepartment(),
                savedUser.getRegion(),
                savedUser.getBranch(),
                savedUser.getTenantId(),
                tenant.getName()
        );
    }

    /**
     * Authenticates a user and generates a JWT token.
     * @param request LoginRequest DTO
     * @return AuthResponse DTO containing the JWT token.
     */
    public LoginResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmailWithTenant(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.email()));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        String token = jwtService.generateToken(authentication);
        return new LoginResponse(token, user);
    }

    /**
     * Checks for similar tenant names.
     *
     * @param request The check tenant request details.
     * @return A response containing a list of similar tenant names.
     */
    @Transactional()
    public CheckTenantResponse checkTenantNameSimilarity(CheckTenantRequest request) {
        List<String> similarTenantNames = tenantService.findSimilarTenants(request.tenantName())
                .stream()
                .map(TenantResponse::name)
                .collect(Collectors.toList());
        return new CheckTenantResponse(similarTenantNames);
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
        if (request.organization() == null) { // Individual sign-up
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
     * Resolves the tenant based on the provided organization name.
     * If the organization name is null (indicating an individual sign-up), it defaults to the 'www' tenant.
     * Otherwise, it attempts to find an existing tenant or create a new one if it doesn't exist.
     *
     * @param organization The name of the organization, or null for individual sign-ups.
     * @return The resolved Tenant entity.
     * @throws EntityNotFoundException If the default 'www' tenant is not found during an individual sign-up.
     */
    private Tenant resolveTenant(String organization) {
        if (organization == null) {
            // For individual sign-up, assume 'www' tenant exists and find it.
            return tenantRepository.findBySlug("www")
                    .orElseThrow(() -> new EntityNotFoundException("Default 'www' tenant not found. Please ensure it is initialized."));
        }

        return tenantService.findOrCreateTenant(organization);
    }

    /**
     * Authenticates a SUPER_ADMIN user and generates a JWT token.
     * Throws an AccessDeniedException if the user does not have the SUPER_ADMIN role.
     * @param request LoginRequest DTO
     * @return LoginResponse DTO containing the JWT token and user details.
     */
    public LoginResponse loginSuperAdmin(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = (User) authentication.getPrincipal();

        // Verify the user has the SUPER_ADMIN role
        boolean isSuperAdmin = user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isSuperAdmin) {
            throw new AccessDeniedException("Access Denied: User is not a system admin.");
        }

        String token = jwtService.generateToken(authentication);
        return new LoginResponse(token, user);
    }

    /**
     * Performs logout by clearing the security context.
     */
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}