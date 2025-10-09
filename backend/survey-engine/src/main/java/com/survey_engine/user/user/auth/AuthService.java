package com.user_service.user_service.user.auth;

import com.user_service.user_service.company.Company;
import com.user_service.user_service.company.CompanyRepository;
import com.user_service.user_service.config.security.JWTService;
import com.user_service.user_service.user.User;
import com.user_service.user_service.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

/**
 * Business logic for authentication service
 * Uses AuthenticationManager to authenticate user
 * Uses generateToken() method from JWTService to generate JWT token
 * @see com.user_service.user_service.config.security.SecurityConfig
 * @see JWTService
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CompanyRepository companyRepository;

    /**
     * Registers new user and generates token
     * @param request AuthRequest DTO
     * @return AuthResponse DTO
     */
    @Transactional
    public AuthResponse registerUser(SignUpRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DataIntegrityViolationException("Email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        if (request.role() == null) {
            user.setRole("Regular");
        } else {
            user.setRole(request.role());
        }

        if (request.companyId() != null) {
            Company company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new EntityNotFoundException("Company not found"));
            user.setCompany(company);
        }

        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    /**
     * Authenticates user and generates token
     * AuthenticationManager is an interface; ite defers to AuthenticationProvider declared in SecurityConfig
     * @param request AuthRequest DTO
     * @return AuthResponse DTO
     */
    @Transactional
    public AuthResponse authenticateUser(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}
