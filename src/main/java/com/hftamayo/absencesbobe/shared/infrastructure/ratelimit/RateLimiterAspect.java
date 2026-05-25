package com.hftamayo.absencesbobe.shared.infrastructure.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import com.hftamayo.absencesbobe.shared.web.error.RateLimiterError;
import io.github.bucket4j.Bucket;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Aspect for rate limiting method calls using AOP.
 * Intercepts methods annotated with @RateLimit and applies token bucket rate limiting.
 */
@Aspect
@Component
@AllArgsConstructor
public class RateLimiterAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterAspect.class);
    private static final String DEFAULT_USER_ROLE = "ANONYMOUS";

    private final RateLimiterUtil rateLimiterUtil;
    private final RateLimiterConfig rateLimiterConfig;
    private final ObjectMapper objectMapper;

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Around advice that intercepts methods annotated with @RateLimit.
     *
     * @param joinPoint The join point representing the method call
     * @return The result of the method execution or rate limit error response
     * @throws Throwable If an error occurs during execution
     */
    @Around("@annotation(com.hftamayo.absencesbobe.shared.infrastructure.ratelimit.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get request context
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RateLimiterError("Request context not available");
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        if (request == null || response == null) {
            throw new RateLimiterError("HTTP request/response not available");
        }

        // Get method and annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimitAnnotation = method.getAnnotation(RateLimit.class);

        if (rateLimitAnnotation == null) {
            // No annotation found, proceed with method execution
            return joinPoint.proceed();
        }

        // Extract configuration
        String endpoint = request.getRequestURI();
        String userRole = extractUserRole(request);
        long tokensToConsume = rateLimitAnnotation.tokens();

        try {
            // Get combined configuration for endpoint and user
            RateLimiterConfig config = rateLimiterConfig.getCombinedConfig(endpoint, userRole);

            String bucketKey = endpoint + ":" + userRole;
            Bucket bucket = buckets.computeIfAbsent(bucketKey, ignored -> rateLimiterUtil.createBucket(config));

            // Try to consume tokens
            boolean consumed = rateLimiterUtil.tryConsume(bucket, tokensToConsume);

            if (consumed) {
                // Tokens consumed successfully, set headers and proceed
                setRateLimitHeaders(response, bucket, config);
                logger.debug("Rate limit check passed for endpoint: {}, user: {}, tokens consumed: {}",
                        endpoint, userRole, tokensToConsume);
                return joinPoint.proceed();
            } else {
                // Rate limit exceeded, return error response
                logger.warn("Rate limit exceeded for endpoint: {}, user: {}, requested tokens: {}",
                        endpoint, userRole, tokensToConsume);
                return createRateLimitErrorResponse(response, bucket, config);
            }

        } catch (Exception e) {
            logger.error("Error during rate limiting for endpoint: {}, user: {}", endpoint, userRole, e);
            throw new RateLimiterError("Rate limiting error", e);
        }
    }

    /**
     * Extracts user role from the Authorization header.
     *
     * @param request The HTTP request
     * @return The user role or default role if not found
     */
    private String extractUserRole(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader == null || authorizationHeader.trim().isEmpty()) {
            return DEFAULT_USER_ROLE;
        }

        // Simple role extraction - in a real application, you would decode the JWT
        // and extract the role from the token claims
        if (authorizationHeader.contains("admin") || authorizationHeader.contains("ADMIN")) {
            return "ADMIN";
        } else if (authorizationHeader.contains("user") || authorizationHeader.contains("USER")) {
            return "USER";
        } else {
            return DEFAULT_USER_ROLE;
        }
    }

    /**
     * Sets rate limit headers in the HTTP response.
     *
     * @param response The HTTP response
     * @param bucket The token bucket
     * @param config The rate limiter configuration
     */
    private void setRateLimitHeaders(HttpServletResponse response, Bucket bucket, RateLimiterConfig config) {
        long availableTokens = rateLimiterUtil.getAvailableTokens(bucket);
        long capacity = config.getCapacity();

        response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(availableTokens));
        response.setHeader("X-RateLimit-Reset", getResetTime(config.getRefillDuration()));
    }

    /**
     * Creates a rate limit error response.
     *
     * @param response The HTTP response
     * @param bucket The token bucket
     * @param config The rate limiter configuration
     * @return The error response object
     */
    private Object createRateLimitErrorResponse(HttpServletResponse response, Bucket bucket, RateLimiterConfig config) {
        try {
            // Set response status and content type
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");

            // Create error response using our envelope pattern
            ApiResponseDto<Void> envelopeResponse = ApiResponseDto.fail(
                    ErrorApiResponse.RATE_LIMITED,
                    null
            );

            // Set rate limit headers even for error responses
            setRateLimitHeaders(response, bucket, config);

            // Write response to output stream
            String jsonResponse = objectMapper.writeValueAsString(envelopeResponse);
            response.getWriter().write(jsonResponse);

            return null; // Return null to prevent further processing

        } catch (IOException e) {
            logger.error("Error writing rate limit error response", e);
            throw new RateLimiterError("Error creating rate limit response", e);
        }
    }

    /**
     * Calculates the reset time for rate limit headers.
     *
     * @param refillDuration The refill duration
     * @return The reset time as a timestamp string
     */
    private String getResetTime(java.time.Duration refillDuration) {
        // Calculate reset time based on current time + refill duration
        long resetTime = System.currentTimeMillis() + refillDuration.toMillis();
        return String.valueOf(resetTime);
    }
} 