/**
 * Performance Survey Module
 * <p>
 * This module handles the "competition/performance" based surveys.
 * It acts as an extension to the core survey module, adding scoring,
 * hierarchical aggregation, and gamification.
 * </p>
 */
@ApplicationModule(
        allowedDependencies = { "common :: *", "user", "survey" }
)
package com.survey_engine.performance_survey;

import org.springframework.modulith.ApplicationModule;