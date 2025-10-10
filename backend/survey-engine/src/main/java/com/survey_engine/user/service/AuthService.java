package com.survey_engine.user.service;

import com.survey_engine.user.dto.SignUpRequest;
import com.survey_engine.user.dto.UserResponse;
import com.survey_engine.user.models.Company;
import com.survey_engine.user.repository.CompanyRepository;
import com.survey_engine.user.repository.UserRepository;
import com.survey_engine.user.models.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

/**
 * Business logic for authentication service
 * Uses AuthenticationManager to authenticate user
 * Uses generateToken() method from JWTService to generate JWT token
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;

    /**
     * Registers new user and generates token
     * @param request AuthRequest DTO
     * @return AuthResponse DTO
     */
    @Transactional
    public UserResponse registerUser(SignUpRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DataIntegrityViolationException("Email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        if (request.role() == null) {
            user.setRole("REGULAR");
        } else {
            user.setRole(request.role());
        }

        if (request.companyId() != null) {
            Company company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new EntityNotFoundException("Company not found"));
            user.setCompany(company);
        }

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCompany() != null ? savedUser.getCompany().getId() : null
        );
    }
}
