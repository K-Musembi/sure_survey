package com.survey_engine.common.auditing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that require auditing.
 * When a method annotated with {@code @Auditable} is executed,
 * an audit log entry will be created.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    /**
     * A description of the auditable action.
     * @return The description of the action.
     */
    String action() default "";
}