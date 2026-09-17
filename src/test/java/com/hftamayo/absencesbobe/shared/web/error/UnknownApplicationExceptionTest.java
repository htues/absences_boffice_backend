package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class UnknownApplicationExceptionTest {

    @Test
    void constructor_withMessage_setsMessageAndDescriptorValues() {
        UnknownApplicationException error = new UnknownApplicationException("Something went wrong");

        assertEquals("Something went wrong", error.getMessage());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR, error.getType());
        assertEquals("Something went wrong", error.getDetail());
        assertEquals("UNKNOWN_ERROR", error.getErrorCode());
        assertEquals(500, error.getStatusCode());
        assertEquals("UNKNOWN_ERROR", error.getMessageKey());
    }

    @Test
    void constructor_withMessageAndCause_setsCauseAndMessage() {
        Throwable cause = new IllegalStateException("root cause");
        UnknownApplicationException error = new UnknownApplicationException("Something went wrong", cause);

        assertEquals("Something went wrong", error.getMessage());
        assertSame(cause, error.getCause());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR, error.getType());
        assertEquals("Something went wrong", error.getDetail());
    }
}
