package com.survey_engine.user.controller;

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
 * @see com.user_service.user_service.user.auth.AuthService
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final com.user_service.user_service.user.auth.AuthService authService;

    /**
     * Method to create user using HTTP POST method
     * @param request - The sign-up request details.
     * @return - An authentication response containing the JWT token.
     */
    @PostMapping("/signup")
    public ResponseEntity<com.user_service.user_service.user.auth.AuthResponse> registerUser(@Valid @RequestBody com.user_service.user_service.user.auth.SignUpRequest request) {
        com.user_service.user_service.user.auth.AuthResponse responseObject = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseObject);
    }

    /**
     * Method to login existing user using HTTP POST method
     * @param request - The login request details.
     * @return - An authentication response containing the JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<com.user_service.user_service.user.auth.AuthResponse> authenticateUser(@Valid @RequestBody com.user_service.user_service.user.auth.LoginRequest request) {
        com.user_service.user_service.user.auth.AuthResponse responseObject = authService.authenticateUser(request);
        return ResponseEntity.ok().body(responseObject);
    }
}
