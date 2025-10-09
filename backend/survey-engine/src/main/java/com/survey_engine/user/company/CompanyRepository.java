package com.survey_engine.user.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Company entity.
 * Provides CRUD operations and custom queries for companies.
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);
}

//findById(), findAll(), etc. provided by default through JpaRepository