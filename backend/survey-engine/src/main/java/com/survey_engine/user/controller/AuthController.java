package com.survey_engine.user.controller;

import com.survey_engine.common.auditing.Auditable;
import com.survey_engine.user.dto.*;
import com.survey_engine.user.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP endpoints for registering new users and authenticating existing users.
 * Two-token strategy:
 *   - Access token → 15-minute JWT, set as HttpOnly cookie "access_token"
 *   - Refresh token → Opaque UUID, set as HttpOnly cookie "refresh_token"
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.expiration:15}")
    private long jwtExpirationMinutes;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody SignUpRequest request) {
        UserResponse responseObject = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseObject);
    }

    @PostMapping("/check-tenant")
    public ResponseEntity<CheckTenantResponse> checkTenant(@Valid @RequestBody CheckTenantRequest request) {
        CheckTenantResponse responseObject = authService.checkTenantNameSimilarity(request);
        return ResponseEntity.ok(responseObject);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> loginUser(@Valid @RequestBody LoginRequest request,
                                                  HttpServletResponse response) {
        LoginResponse loginResponse = authService.loginUser(request);
        setAccessCookie(response, loginResponse.token());
        setRefreshCookie(response, loginResponse.refreshToken());

        UserResponse userResponse = new UserResponse(
                loginResponse.user().getId(),
                loginResponse.user().getName(),
                loginResponse.user().getEmail(),
                loginResponse.user().getDepartment(),
                loginResponse.user().getRegion(),
                loginResponse.user().getBranch(),
                loginResponse.user().getTenantId(),
                loginResponse.user().getTenant().getName()
        );

        return ResponseEntity.ok(userResponse);
    }

    /**
     * Refresh endpoint: reads the refresh_token cookie, validates it, rotates it,
     * and issues a new access token + new refresh token via cookies.
     * No request body needed — everything travels via cookies.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshToken(HttpServletRequest request,
                                             HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refresh_token");
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TokenRefreshResult result = authService.refreshAccessToken(refreshToken);
        setAccessCookie(response, result.accessToken());
        setRefreshCookie(response, result.refreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @Auditable(action = "USER_LOGOUT")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refresh_token");
        authService.logout(refreshToken);
        clearCookie(response, "access_token");
        clearCookie(response, "refresh_token");
        return ResponseEntity.ok().build();
    }

    // ── Cookie helpers ────────────────────────────────────────────────────────

    private void setAccessCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        // cookie.setSecure(true); // enable in production
        cookie.setMaxAge((int) (jwtExpirationMinutes * 60));
        response.addCookie(cookie);
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/v1/auth/refresh"); // restrict refresh cookie to the refresh endpoint
        // cookie.setSecure(true); // enable in production
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}
