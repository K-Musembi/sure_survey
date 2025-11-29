/**
 * Billing module for handling subscriptions, plans, and invoices.
 */
@ApplicationModule(
        allowedDependencies = {"common :: *", "user", "survey"}
)
package com.survey_engine.billing;

import org.springframework.modulith.ApplicationModule;
