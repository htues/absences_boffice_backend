package com.hftamayo.absencesbobe.shared.infrastructure.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RateLimiterUtilTest {

    @Test
    @DisplayName("createBucket(config): builds a bucket with the provided configuration")
    void createBucket_withConfig_buildsBucket() {
        RateLimiterUtil util = new RateLimiterUtil(defaultConfig(10L, 2L, Duration.ofMinutes(1)));
        RateLimiterConfig config = defaultConfig(4L, 1L, Duration.ofSeconds(30));

        Bucket bucket = util.createBucket(config);

        assertNotNull(bucket);
        assertTrue(bucket.tryConsume(4));
        assertFalse(bucket.tryConsume(1));
    }

    @Test
    @DisplayName("createBucket(config): rejects null config")
    void createBucket_withNullConfig_throws() {
        RateLimiterUtil util = new RateLimiterUtil(defaultConfig(10L, 2L, Duration.ofMinutes(1)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> util.createBucket((RateLimiterConfig) null)
        );

        assertEquals("RateLimiterConfig cannot be null", ex.getMessage());
    }

    @Test
    @DisplayName("createDefaultBucket: uses the injected default configuration")
    void createDefaultBucket_usesDefaultConfig() {
        RateLimiterConfig defaultConfig = defaultConfig(7L, 3L, Duration.ofMinutes(2));
        RateLimiterUtil util = new RateLimiterUtil(defaultConfig);

        Bucket bucket = util.createDefaultBucket();

        assertNotNull(bucket);
        assertTrue(bucket.tryConsume(7));
        assertFalse(bucket.tryConsume(1));
    }

    @Test
    @DisplayName("createBucket(capacity, refillRate, duration): builds a bucket after validation")
    void createBucket_withParameters_buildsBucket() {
        RateLimiterUtil util = new RateLimiterUtil(defaultConfig(10L, 2L, Duration.ofMinutes(1)));

        Bucket bucket = util.createBucket(3L, 2L, Duration.ofSeconds(10));

        assertNotNull(bucket);
        assertTrue(bucket.tryConsume(3));
        assertFalse(bucket.tryConsume(1));
    }

    @Test
    @DisplayName("createBucket(capacity, refillRate, duration): rejects invalid parameters")
    void createBucket_withInvalidParameters_throws() {
        RateLimiterUtil util = new RateLimiterUtil(defaultConfig(10L, 2L, Duration.ofMinutes(1)));

        IllegalArgumentException capacityEx = assertThrows(
                IllegalArgumentException.class,
                () -> util.createBucket(0L, 1L, Duration.ofSeconds(1))
        );
        assertEquals("Capacity must be greater than 0", capacityEx.getMessage());

        IllegalArgumentException refillRateEx = assertThrows(
                IllegalArgumentException.class,
                () -> util.createBucket(1L, 0L, Duration.ofSeconds(1))
        );
        assertEquals("Refill rate must be greater than 0", refillRateEx.getMessage());

        IllegalArgumentException durationEx = assertThrows(
                IllegalArgumentException.class,
                () -> util.createBucket(1L, 1L, Duration.ZERO)
        );
        assertEquals("Refill duration must be positive", durationEx.getMessage());
    }

    @Test
    @DisplayName("createBucketWithBandwidth: builds a bucket from the provided bandwidth")
    void createBucketWithBandwidth_buildsBucket() {
        RateLimiterUtil util = new RateLimiterUtil(defaultConfig(10L, 2L, Duration.ofMinutes(1)));
        Bandwidth bandwidth = Bandwidth.classic(5, io.github.bucket4j.Refill.intervally(1, Duration.ofSeconds(15)));

        Bucket bucket = util.createBucketWithBandwidth(bandwidth);

        assertNotNull(bucket);
        assertTrue(bucket.tryConsume(5));
        assertFalse(bucket.tryConsume(1));
    }

    @Test
    @DisplayName("createBucketWithBandwidth: rejects null bandwidth")
    void createBucketWithBandwidth_nullBandwidth_throws() {
        RateLimiterUtil util = new RateLimiterUtil(defaultConfig(10L, 2L, Duration.ofMinutes(1)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> util.createBucketWithBandwidth(null)
        );

        assertEquals("Bandwidth cannot be null", ex.getMessage());
    }

    @Test
    @DisplayName("tryConsume: delegates to the bucket and handles edge cases")
    void tryConsume_delegatesAndHandlesEdges() {
        RateLimiterUtil util = new RateLimiterUtil(defaultConfig(10L, 2L, Duration.ofMinutes(1)));
        Bucket bucket = mock(Bucket.class);

        when(bucket.tryConsume(3L)).thenReturn(true);

        assertTrue(util.tryConsume(bucket, 3L));
        assertTrue(util.tryConsume(bucket, 0L));
        assertFalse(util.tryConsume(bucket, -1L));

        verify(bucket).tryConsume(3L);
        verifyNoMoreInteractions(bucket);
    }

    @Test
    @DisplayName("tryConsume: rejects null bucket")
    void tryConsume_nullBucket_throws() {
        RateLimiterUtil util = new RateLimiterUtil(defaultConfig(10L, 2L, Duration.ofMinutes(1)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> util.tryConsume(null, 1L)
        );

        assertEquals("Bucket cannot be null", ex.getMessage());
    }

    @Test
    @DisplayName("getAvailableTokens: delegates to the bucket")
    void getAvailableTokens_delegates() {
        RateLimiterUtil util = new RateLimiterUtil(defaultConfig(10L, 2L, Duration.ofMinutes(1)));
        Bucket bucket = mock(Bucket.class);
        when(bucket.getAvailableTokens()).thenReturn(8L);

        long available = util.getAvailableTokens(bucket);

        assertEquals(8L, available);
        verify(bucket).getAvailableTokens();
        verifyNoMoreInteractions(bucket);
    }

    @Test
    @DisplayName("getAvailableTokens: rejects null bucket")
    void getAvailableTokens_nullBucket_throws() {
        RateLimiterUtil util = new RateLimiterUtil(defaultConfig(10L, 2L, Duration.ofMinutes(1)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> util.getAvailableTokens(null)
        );

        assertEquals("Bucket cannot be null", ex.getMessage());
    }

    @Test
    @DisplayName("default getters: expose the injected defaults")
    void defaultGetters_returnInjectedValues() {
        RateLimiterConfig defaultConfig = defaultConfig(11L, 4L, Duration.ofSeconds(45));
        RateLimiterUtil util = new RateLimiterUtil(defaultConfig);

        assertEquals(11L, util.getDefaultCapacity());
        assertEquals(4L, util.getDefaultRefillRate());
        assertEquals(Duration.ofSeconds(45), util.getDefaultRefillDuration());
    }

    private static RateLimiterConfig defaultConfig(Long capacity, Long refillRate, Duration refillDuration) {
        RateLimiterConfig config = new RateLimiterConfig();
        config.setCapacity(capacity);
        config.setRefillRate(refillRate);
        config.setRefillDuration(refillDuration);
        return config;
    }
}
