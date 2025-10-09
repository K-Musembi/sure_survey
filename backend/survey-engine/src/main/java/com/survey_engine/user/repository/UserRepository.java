package com.survey_engine.user.repository;

import com.survey_engine.user.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom queries for users.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByCompanyId(Long companyId);
}

//findById(), findAll(), etc. provided by default through JpaRepository
