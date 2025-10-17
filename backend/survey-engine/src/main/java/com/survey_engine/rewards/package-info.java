/**
 * Rewards module
 */

@ApplicationModule(
        allowedDependencies = { "common :: *", "user", "survey" }
)

package com.survey_engine.rewards;

import org.springframework.modulith.ApplicationModule;