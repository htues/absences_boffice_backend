package com.hftamayo.absencesbobe.shared.infrastructure.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class RateLimitTest {

    @Test
    @DisplayName("annotation: is retained at runtime and targets methods")
    void annotation_hasRuntimeRetentionAndMethodTarget() {
        Retention retention = RateLimit.class.getAnnotation(Retention.class);
        Target target = RateLimit.class.getAnnotation(Target.class);

        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());

        assertNotNull(target);
        assertArrayEquals(new ElementType[]{ElementType.METHOD}, target.value());
    }

    @Test
    @DisplayName("annotation: exposes the documented default values")
    void annotation_exposesDefaultValues() throws NoSuchMethodException {
        Method method = SampleHandlers.class.getDeclaredMethod("defaultLimited");
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        assertNotNull(rateLimit);
        assertEquals(1L, rateLimit.tokens());
        assertEquals("", rateLimit.key());
    }

    @Test
    @DisplayName("annotation: preserves custom token and key values")
    void annotation_preservesCustomValues() throws NoSuchMethodException {
        Method method = SampleHandlers.class.getDeclaredMethod("customLimited");
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        assertNotNull(rateLimit);
        assertEquals(7L, rateLimit.tokens());
        assertEquals("company.create", rateLimit.key());
    }

    private static final class SampleHandlers {
        @RateLimit
        @SuppressWarnings("unused")
        private void defaultLimited() {
        }

        @RateLimit(tokens = 7, key = "company.create")
        @SuppressWarnings("unused")
        private void customLimited() {
        }
    }
}
