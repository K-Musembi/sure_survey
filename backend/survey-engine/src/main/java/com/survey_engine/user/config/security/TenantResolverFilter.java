package com.survey_engine.user.config.security;

import com.survey_engine.user.models.Tenant;
import com.survey_engine.user.repository.TenantRepository;
import com.survey_engine.user.service.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * A Spring Web filter that resolves the tenant ID from the request's hostname (subdomain) or a request header.
 * It extracts the subdomain, looks up the corresponding tenant in the database, and sets the
 * tenant ID in the {@link TenantContext} for the duration of the request.
 */
@Component
public class TenantResolverFilter extends OncePerRequestFilter {

    private final TenantRepository tenantRepository;
    private final JwtDecoder jwtDecoder;

    /**
     * Constructs a new {@code TenantResolverFilter}.
     * This constructor is used for dependency injection by Spring.
     *
     * @param tenantRepository The repository for accessing tenant data.
     * @param jwtDecoder The {@link JwtDecoder} used for decoding JWTs to extract tenant information.
     * The {@code @Lazy} annotation is used here to break a potential circular dependency during bean initialization.
     * It ensures that a proxy for {@code JwtDecoder} is injected initially, and the actual {@code JwtDecoder}
     * bean is only fully initialized and resolved when it's first accessed,
     * thereby preventing a deadlock during application startup.
     */
    @Autowired
    public TenantResolverFilter(TenantRepository tenantRepository, @Lazy JwtDecoder jwtDecoder) {
        this.tenantRepository = tenantRepository;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Determines whether the filter should not be applied to the current request.
     * This method is overridden to exclude specific authentication-related endpoints
     * from tenant resolution, allowing unauthenticated access for signup and tenant checks.
     *
     * @param request The current HttpServletRequest.
     * @return true if the request URI matches an excluded path (e.g., /api/v1/auth/signup, /api/v1/auth/check-tenant), false otherwise.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/v1/auth/signup") || path.equals("/api/v1/auth/check-tenant") || path.equals("/api/v1/auth/login");
    }

    /**
     * Performs the tenant resolution logic for each incoming request.
     * It attempts to resolve the tenant in the following order:
     * 1. By slug from the subdomain in the request's hostname.
     * 2. By name from the "X-Tenant-Organization" request header.
     * 3. Falls back to a default tenant with the slug "www".
     * Once resolved, it sets the tenant ID in the {@link TenantContext} and ensures
     * the context is cleared after the request is processed.
     *
     * @param request The HTTP servlet request.
     * @param response The HTTP servlet response.
     * @param filterChain The filter chain to proceed with.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Long tenantId = null;

        // 1. Try to resolve from JWT
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                tenantId = jwtDecoder.decode(token).getClaim("tenantId");
            } catch (JwtException e) {
                // Invalid token, security filter will handle it.
            }
        }

        // 2. If not in JWT, try subdomain
        if (tenantId == null) {
            String host = request.getServerName();
            String[] parts = host.split("\\.");
            if (parts.length > 1) {
                String slug = parts[0];
                Optional<Tenant> tenantOptional = tenantRepository.findBySlug(slug);
                if (tenantOptional.isPresent()) {
                    tenantId = tenantOptional.get().getId();
                }
            }
        }

        // 3. If not found, try "X-Tenant-Organization" header
        if (tenantId == null) {
            String tenantName = request.getHeader("X-Tenant-Organization");
            if (tenantName != null && !tenantName.isEmpty()) {
                Optional<Tenant> tenantOptional = tenantRepository.findByName(tenantName);
                if (tenantOptional.isPresent()) {
                    tenantId = tenantOptional.get().getId();
                }
            }
        }

        // 4. If still not found, fall back to "www"
        if (tenantId == null) {
            Optional<Tenant> tenantOptional = tenantRepository.findBySlug("www");
            if (tenantOptional.isPresent()) {
                tenantId = tenantOptional.get().getId();
            }
        }

        if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}