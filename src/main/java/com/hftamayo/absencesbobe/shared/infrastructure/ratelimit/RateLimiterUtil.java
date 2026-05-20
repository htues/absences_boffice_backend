package com.hftamayo.absencesbobe.shared.infrastructure.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Utility class for rate limiting operations using Bucket4j token bucket algorithm.
 * Provides methods for creating buckets, consuming tokens, and managing rate limits.
 */
@Component
public class RateLimiterUtil {

    private final RateLimiterConfig defaultConfig;

    public RateLimiterUtil(RateLimiterConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    /**
     * Creates a bucket with the specified configuration.
     *
     * @param config The rate limiter configuration
     * @return A configured Bucket instance
     * @throws IllegalArgumentException if config is null
     */
    public Bucket createBucket(RateLimiterConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("RateLimiterConfig cannot be null");
        }

        Bandwidth bandwidth = Bandwidth.classic(
                config.getCapacity(),
                Refill.intervally(config.getRefillRate(), config.getRefillDuration())
        );

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * Creates a bucket with default configuration.
     *
     * @return A Bucket instance with default settings
     */
    public Bucket createDefaultBucket() {
        return createBucket(defaultConfig);
    }

    /**
     * Creates a bucket with custom parameters.
     *
     * @param capacity The maximum number of tokens the bucket can hold
     * @param refillRate The number of tokens to refill
     * @param refillDuration The duration between refills
     * @return A configured Bucket instance
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Bucket createBucket(long capacity, long refillRate, Duration refillDuration) {
        validateBucketParameters(capacity, refillRate, refillDuration);

        Bandwidth bandwidth = Bandwidth.classic(capacity, Refill.intervally(refillRate, refillDuration));

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * Creates a bucket with a custom bandwidth configuration.
     *
     * @param bandwidth The custom bandwidth configuration
     * @return A Bucket instance with the specified bandwidth
     * @throws IllegalArgumentException if bandwidth is null
     */
    public Bucket createBucketWithBandwidth(Bandwidth bandwidth) {
        if (bandwidth == null) {
            throw new IllegalArgumentException("Bandwidth cannot be null");
        }

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * Attempts to consume the specified number of tokens from the bucket.
     *
     * @param bucket The bucket to consume tokens from
     * @param tokens The number of tokens to consume
     * @return true if tokens were successfully consumed, false otherwise
     * @throws IllegalArgumentException if bucket is null or tokens is negative
     */
    public boolean tryConsume(Bucket bucket, long tokens) {
        if (bucket == null) {
            throw new IllegalArgumentException("Bucket cannot be null");
        }

        if (tokens < 0) {
            return false;
        }

        if (tokens == 0) {
            return true; // Consuming 0 tokens always succeeds
        }

        return bucket.tryConsume(tokens);
    }

    /**
     * Gets the number of available tokens in the bucket.
     *
     * @param bucket The bucket to check
     * @return The number of available tokens
     * @throws IllegalArgumentException if bucket is null
     */
    public long getAvailableTokens(Bucket bucket) {
        if (bucket == null) {
            throw new IllegalArgumentException("Bucket cannot be null");
        }

        return bucket.getAvailableTokens();
    }

    /**
     * Validates bucket creation parameters.
     *
     * @param capacity The bucket capacity
     * @param refillRate The refill rate
     * @param refillDuration The refill duration
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private void validateBucketParameters(long capacity, long refillRate, Duration refillDuration) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        if (refillRate <= 0) {
            throw new IllegalArgumentException("Refill rate must be greater than 0");
        }

        if (refillDuration == null || refillDuration.isZero() || refillDuration.isNegative()) {
            throw new IllegalArgumentException("Refill duration must be positive");
        }
    }

    /**
     * Gets the default capacity value.
     *
     * @return The default capacity
     */
    public long getDefaultCapacity() {
        return defaultConfig.getCapacity();
    }

    /**
     * Gets the default refill rate value.
     *
     * @return The default refill rate
     */
    public long getDefaultRefillRate() {
        return defaultConfig.getRefillRate();
    }

    /**
     * Gets the default refill duration value.
     *
     * @return The default refill duration
     */
    public Duration getDefaultRefillDuration() {
        return defaultConfig.getRefillDuration();
    }
} 