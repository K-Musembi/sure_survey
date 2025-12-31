/**
 * Module for handling external business integrations (M-Pesa, POS).
 */
@ApplicationModule(
        allowedDependencies = {"common :: *", "user", "survey", "billing"}
)
package com.survey_engine.business_integration;

import org.springframework.modulith.ApplicationModule;