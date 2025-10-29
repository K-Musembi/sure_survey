package com.survey_engine.user.controller;

import com.survey_engine.user.dto.*;
import com.survey_engine.user.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP endpoints for registering new users and authenticating existing users
 * @see com.survey_engine.user.service.AuthService
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    /**
     * Method to create user using HTTP POST method
     * @param request - The sign-up request details.
     * @return - An authentication response containing the JWT token.
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody SignUpRequest request) {
        UserResponse responseObject = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseObject);
    }

    /**
     * Method to check for similar tenant names.
     * @param request - The check tenant request details.
     * @return - A response containing a list of similar tenant names.
     */
    @PostMapping("/check-tenant")
    public ResponseEntity<CheckTenantResponse> checkTenant(@Valid @RequestBody CheckTenantRequest request) {
        CheckTenantResponse responseObject = authService.checkTenantNameSimilarity(request);
        return ResponseEntity.ok(responseObject);
    }

    /**
     * Method to authenticate user and provide a JWT token.
     * @param request - The login request details (email and password).
     * @return - An authentication response containing the JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> loginUser(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.loginUser(request);
        response.addCookie(createCookie(loginResponse.token()));

        UserResponse userResponse = new UserResponse(
                loginResponse.user().getId(),
                loginResponse.user().getName(),
                loginResponse.user().getEmail(),
                loginResponse.user().getDepartment(),
                loginResponse.user().getTenantId(),
                loginResponse.user().getTenant().getName()
        );

        return ResponseEntity.ok(userResponse);
    }

    private Cookie createCookie(String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        // cookie.setSecure(true); // Uncomment in production
        cookie.setMaxAge(60 * 60 * 24); // 1 day
        return cookie;
    }
}
