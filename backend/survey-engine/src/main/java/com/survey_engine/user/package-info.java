/**
 * User module
 */

@ApplicationModule(
        allowedDependencies = {"common :: *", "survey"}
)

package com.survey_engine.user;

import org.springframework.modulith.ApplicationModule;