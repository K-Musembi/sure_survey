/**
 * Decision Intelligence Platform module.
 * Generates AI-powered insight reports and action plans from survey data.
 * Cross-module access through IntelligenceApi (@NamedInterface "intelligence").
 */
@ApplicationModule(
    allowedDependencies = {"survey", "ai_analysis", "user", "common :: *"}
)
package com.survey_engine.intelligence;

import org.springframework.modulith.ApplicationModule;
