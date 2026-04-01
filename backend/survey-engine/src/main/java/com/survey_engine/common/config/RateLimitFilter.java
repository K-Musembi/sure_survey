package com.survey_engine.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for public API endpoints.
 * Limits requests per IP address using a sliding window approach.
 * Rates:
 * - Auth endpoints: 20 requests per minute
 * - Public endpoints: 60 requests per minute
 * - All other: no filter (authenticated endpoints have their own guards)
 * For production, this should be replaced with a Redis-backed or API gateway solution.
 */
@Component
@Order(0) // Run before security filters
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int AUTH_LIMIT = 20;
    private static final int PUBLIC_LIMIT = 60;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final Map<String, RateBucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        int limit = resolveLimit(path);

        if (limit <= 0) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String bucketKey = clientIp + ":" + (path.startsWith("/api/v1/auth") ? "auth" : "public");

        RateBucket bucket = buckets.compute(bucketKey, (key, existing) -> {
            long now = System.currentTimeMillis();
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                return new RateBucket(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (bucket.count.get() > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Rate limit exceeded. Try again later.\",\"value\":\"RATE_LIMITED\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Only rate-limit public-facing endpoints
        return !path.startsWith("/api/v1/auth")
                && !path.startsWith("/r/")
                && !path.startsWith("/api/v1/referrals/subjects")
                && !path.startsWith("/api/v1/billing/plans");
    }

    private int resolveLimit(String path) {
        if (path.startsWith("/api/v1/auth")) return AUTH_LIMIT;
        return PUBLIC_LIMIT;
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Scheduled(fixedRate = 300_000) // Every 5 minutes
    public void cleanupExpiredBuckets() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry ->
                now - entry.getValue().windowStart > WINDOW_MS * 5);
    }

    private record RateBucket(long windowStart, AtomicInteger count) {}
}
