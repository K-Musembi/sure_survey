/**
 * Payments module
 */

@ApplicationModule(
        allowedDependencies = { "common :: * ", "user", "billing" }
)

package com.survey_engine.payments;

import org.springframework.modulith.ApplicationModule;