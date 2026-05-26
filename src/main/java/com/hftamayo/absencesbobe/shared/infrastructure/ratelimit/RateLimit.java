package com.hftamayo.absencesbobe.shared.infrastructure.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable rate limiting on methods.
 * When applied to a method, it will be intercepted by RateLimiterAspect
 * and rate limiting will be applied based on the configuration.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * The number of tokens to consume for this method call.
     * Default is 1 token per call.
     *
     * @return The number of tokens to consume
     */
    long tokens() default 1L;
} 