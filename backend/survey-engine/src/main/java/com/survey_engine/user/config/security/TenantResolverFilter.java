package com.survey_engine.user.config.security;

import com.survey_engine.user.models.Tenant;
import com.survey_engine.user.repository.TenantRepository;
import com.survey_engine.user.service.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * A Spring Web filter that resolves the tenant ID from the request's hostname (subdomain) or a request header.
 * It extracts the subdomain, looks up the corresponding tenant in the database, and sets the
 * tenant ID in the {@link TenantContext} for the duration of the request.
 */
@Component
@RequiredArgsConstructor
public class TenantResolverFilter extends OncePerRequestFilter {

    private final TenantRepository tenantRepository;

    /**
     * Performs the tenant resolution logic for each incoming request.
     * It attempts to resolve the tenant in the following order:
     * 1. By slug from the subdomain in the request's hostname.
     * 2. By name from the "X-Tenant-Organization" request header.
     * 3. Falls back to a default tenant with the slug "www".
     *
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
        String host = request.getServerName();
        String[] parts = host.split("\\.");
        Optional<Tenant> tenantOptional = Optional.empty();

        // 1. Try to resolve tenant by subdomain slug
        if (parts.length > 1) {
            String slug = parts[0];
            tenantOptional = tenantRepository.findBySlug(slug);
        }

        // 2. If not found, try to resolve by "X-Tenant-Organization" header
        if (tenantOptional.isEmpty()) {
            String tenantName = request.getHeader("X-Tenant-Organization");
            if (tenantName != null && !tenantName.isEmpty()) {
                tenantOptional = tenantRepository.findByName(tenantName);
            }
        }

        // 3. If still not found, fall back to the default "www" tenant
        if (tenantOptional.isEmpty()) {
            tenantOptional = tenantRepository.findBySlug("www");
        }

        tenantOptional.ifPresent(tenant -> TenantContext.setTenantId(tenant.getId()));

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}