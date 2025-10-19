/**
 * Payments module
 */

@ApplicationModule(
        allowedDependencies = { "common :: * ", "user" }
)

package com.survey_engine.payments;

import org.springframework.modulith.ApplicationModule;