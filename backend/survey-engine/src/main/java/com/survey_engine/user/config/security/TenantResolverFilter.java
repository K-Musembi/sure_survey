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

/**
 * A Spring Web filter that resolves the tenant ID from the request's hostname (subdomain).
 * It extracts the subdomain, looks up the corresponding tenant in the database, and sets the
 * tenant ID in the {@link TenantContext} for the duration of the request.
 */
@Component
@RequiredArgsConstructor
public class TenantResolverFilter extends OncePerRequestFilter {

    private final TenantRepository tenantRepository;

    /**
     * Performs the tenant resolution logic for each incoming request.
     * Extracts the subdomain from the hostname, attempts to find a matching tenant,
     * sets the tenant ID in the {@link TenantContext}, and ensures the context is cleared
     * after the request is processed.
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

        if (parts.length > 1) {
            String slug = parts[0];
            tenantRepository.findBySlug(slug).ifPresent(tenant -> TenantContext.setTenantId(tenant.getId()));
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}