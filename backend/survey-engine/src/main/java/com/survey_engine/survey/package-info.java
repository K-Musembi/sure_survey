/**
 * Survey module
 */

@ApplicationModule(
        allowedDependencies = { "common :: *", "user", "billing" }
)
package com.survey_engine.survey;

import org.springframework.modulith.ApplicationModule;