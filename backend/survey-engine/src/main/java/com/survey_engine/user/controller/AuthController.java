package com.survey_engine.user.controller;

import com.survey_engine.common.auditing.Auditable;
import com.survey_engine.user.dto.*;
import com.survey_engine.user.service.AuthService;
import com.survey_engine.user.service.EmailVerificationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    private final EmailVerificationService emailVerificationService;

    @Value("${jwt.expiration:15}")
    private long jwtExpirationMinutes;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

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

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@RequestParam String email) {
        emailVerificationService.resendVerification(email);
        return ResponseEntity.ok().build();
    }

    /**
     * Initiates forgot-password flow — sends a reset link to the user's email.
     * Always returns 200 regardless of whether the email exists (prevents enumeration).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
        emailVerificationService.sendPasswordResetEmail(email);
        return ResponseEntity.ok().build();
    }

    /**
     * Resets the user's password using the one-time token from the reset email.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        emailVerificationService.resetPassword(request.token(), request.newPassword());
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
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtExpirationMinutes * 60));
        response.addCookie(cookie);
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/v1/auth/refresh");
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
