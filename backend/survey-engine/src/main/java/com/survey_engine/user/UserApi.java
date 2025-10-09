package com.survey_engine.user;

import org.springframework.modulith.NamedInterface;

/**
 * Interface API for inter-module communication
 * This is a demo file. Spring Application Events is preferred
 */
@NamedInterface
public class UserApi {
    // Define methods accessible by other modules
    // Include 'allowedDependencies = { .. }' in package.info
}
