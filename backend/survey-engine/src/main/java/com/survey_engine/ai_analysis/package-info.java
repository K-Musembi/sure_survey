/**
 * AI services module — provides all LLM capabilities to the platform.
 * Cross-module access is only permitted through AiApi (@NamedInterface "ai").
 */
@ApplicationModule(
    allowedDependencies = {"survey", "common :: *"}
)
package com.survey_engine.ai_analysis;

import org.springframework.modulith.ApplicationModule;
