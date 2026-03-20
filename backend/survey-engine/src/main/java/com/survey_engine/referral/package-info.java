/**
 * Referral engine module.
 * Manages survey referrals (closed/open) and service/product referrals.
 * Cross-module access through ReferralApi (@NamedInterface "referral").
 */
@ApplicationModule(
    allowedDependencies = {"user", "survey", "rewards", "common :: *"}
)
package com.survey_engine.referral;

import org.springframework.modulith.ApplicationModule;
