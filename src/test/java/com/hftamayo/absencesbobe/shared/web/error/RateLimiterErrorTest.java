package com.hftamayo.absencesbobe.shared.web.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RateLimiterErrorTest {

    @Test
    void constructor_withMessage_setsMessage() {
        RateLimiterError error = new RateLimiterError("Rate limit exceeded");

        assertEquals("Rate limit exceeded", error.getMessage());
        assertNull(error.getCause());
    }

    @Test
    void constructor_withMessageAndCause_setsBothFields() {
        Throwable cause = new IllegalStateException("bucket empty");
        RateLimiterError error = new RateLimiterError("Rate limit exceeded", cause);

        assertEquals("Rate limit exceeded", error.getMessage());
        assertSame(cause, error.getCause());
    }

    @Test
    void constructor_withCause_setsDerivedMessageAndCause() {
        Throwable cause = new IllegalArgumentException("invalid request");
        RateLimiterError error = new RateLimiterError(cause);

        assertSame(cause, error.getCause());
        assertEquals(cause.toString(), error.getMessage());
    }

    @Test
    void constructor_withSuppressionAndWritableStackTrace_setsBothFields() {
        Throwable cause = new RuntimeException("boom");
        RateLimiterError error = new RateLimiterError("Rate limit exceeded", cause, false, false);

        assertEquals("Rate limit exceeded", error.getMessage());
        assertSame(cause, error.getCause());
    }
}
