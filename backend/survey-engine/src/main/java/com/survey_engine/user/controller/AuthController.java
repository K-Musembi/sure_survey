package com.survey_engine.user.controller;

import com.survey_engine.user.dto.AuthResponse;
import com.survey_engine.user.dto.LoginRequest;
import com.survey_engine.user.dto.SignUpRequest;
import com.survey_engine.user.dto.UserResponse;
import com.survey_engine.user.service.AuthService;
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
     * Method to authenticate user and provide a JWT token.
     * @param request - The login request details (email and password).
     * @return - An authentication response containing the JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.loginUser(request);
        return ResponseEntity.ok(authResponse);
    }
}
