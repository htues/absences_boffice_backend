package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErrorLogEventDescriptorTest {

    @Test
    void defaultMethods_delegateToType() {
        ErrorLogEventDescriptor descriptor = new ErrorLogEventDescriptor() {
            @Override
            public ErrorApiResponse getType() {
                return ErrorApiResponse.RATE_LIMITED;
            }

            @Override
            public String getDetail() {
                return "Too many requests";
            }
        };

        assertEquals(ErrorApiResponse.RATE_LIMITED, descriptor.getType());
        assertEquals("Too many requests", descriptor.getDetail());
        assertEquals("RATE_LIMITED", descriptor.getErrorCode());
        assertEquals(429, descriptor.getStatusCode());
        assertEquals("RATE_LIMIT_EXCEEDED", descriptor.getMessageKey());
    }
}
