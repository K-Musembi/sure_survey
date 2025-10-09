package com.survey_engine.user.company.dto;

/**
 * Response Data Transfer Object (DTO) for Company entity
 * @param id
 * @param name
 * @param sector
 * @param country
 */
public record CompanyResponse(

        Long id,
        String name,
        String sector,
        String country
) {}
