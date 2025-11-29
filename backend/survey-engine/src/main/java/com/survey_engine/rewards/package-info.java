/**
 * Rewards module
 */

@ApplicationModule(
        allowedDependencies = { "common :: *", "user", "survey", "billing" }
)
package com.survey_engine.rewards;

import org.springframework.modulith.ApplicationModule;