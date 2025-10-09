package com.survey_engine.user.service;

import com.survey_engine.user.models.Company;
import com.survey_engine.user.models.User;
import com.survey_engine.user.repository.CompanyRepository;
import com.survey_engine.user.repository.UserRepository;
import com.user_service.user_service.user.dto.UserRequest;
import com.user_service.user_service.user.dto.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for User entity
 * Defines business logic
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor method
     * @param userRepository - user repository instance
     * @param companyRepository - company repository instance
     */
    @Autowired
    public UserService(UserRepository userRepository, CompanyRepository companyRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * Method to find user by id
     * @param id - user id
     * @return - response DTO
     */
    @Transactional
    public UserResponse findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return mapToUserResponse(user);
    }

    /**
     * Method to find user by email
     * @param email - user email
     * @return - response DTO
     */
    @Transactional
    public UserResponse findUserByEmail(String email) {
        User user =  userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return mapToUserResponse(user);
    }

    /**
     * Method to find companies with similar companyId
     * @param companyId - company id
     * @return - List of response DTOs
     */
    @Transactional
    public List<UserResponse> findUsersByCompanyId(Long companyId) {
        List<User> users = userRepository.findByCompanyId(companyId);

        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Method to update user properties
     * @param id - user id
     * @param userRequest - request DTO
     * @return - response DTO
     */
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User savedUser = getUser(user, userRequest);
        return mapToUserResponse(savedUser);
    }

    /**
     * Method to delete user (soft delete)
     * @param id - user id
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        userRepository.delete(user);
    }

    /**
     * Method to retrieve user properties and save user in database
     * @param user - user instance
     * @param userRequest - request DTO
     * @return - saved user
     */
    private User getUser(User user, UserRequest userRequest) {
        user.setName(userRequest.name());
        user.setEmail(userRequest.email());
        user.setPassword(passwordEncoder.encode(userRequest.password()));
        if (userRequest.role() != null) {
            user.setRole(userRequest.role());
        }
        if (userRequest.companyId() != null) {
            Company company = companyRepository.findById(userRequest.companyId())
                    .orElseThrow(() -> new EntityNotFoundException("Company Not Found"));
            user.setCompany(company);
        }

        return userRepository.save(user);
    }

    /**
     * Method to map user to response DTO
     * @param user - user instance
     * @return - response DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCompany() != null ? user.getCompany().getId() : null
        );
    }
}
