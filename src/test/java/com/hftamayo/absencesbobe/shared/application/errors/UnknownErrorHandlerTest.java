package com.hftamayo.absencesbobe.shared.application.errors;

import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnknownErrorHandlerTest {

    @Test
    void catchUnknownError_logsAndReturnsUnknownErrorResult() {
        Logger log = mock(Logger.class);
        Exception ex = new RuntimeException("boom");

        Result<String, ? extends ApiResponseDescriptor> result =
                UnknownErrorHandler.catchUnknownError(log, "activateCompany", 42L, ex);

        assertNotNull(result);
        assertTrue(result.isError());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR, result.error());

        verify(log).error("method={} failed for id={}", "activateCompany", 42L, ex);
        verifyNoMoreInteractions(log);
    }
}