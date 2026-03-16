package com.hftamayo.absencesbobe.features.shared.web.factory;

import com.hftamayo.absencesbobe.shared.web.constants.CorrelationConstants;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ErrorLogEventDto;
import com.hftamayo.absencesbobe.shared.web.error.ErrorLogEventDescriptor;
import com.hftamayo.absencesbobe.shared.web.factory.ErrorLogEventFactory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ErrorLogEventFactoryTest {

    @Test
    void mapErrorLogEvent_withValidInputs_returnsErrorLogEventDto() {
        ErrorLogEventDescriptor errorDescriptor = mock(ErrorLogEventDescriptor.class);
        when(errorDescriptor.getMessageKey()).thenReturn("ENTITY_NOT_FOUND");
        when(errorDescriptor.getStatusCode()).thenReturn(404);
        when(errorDescriptor.getDetail()).thenReturn("Company with id 123 was not found");
        when(errorDescriptor.getErrorCode()).thenReturn("NOT_FOUND");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/companies/123");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-id-123");

        ErrorLogEventDto result = ErrorLogEventFactory.mapErrorLogEvent(
                CompanyController.class,
                errorDescriptor,
                request
        );

        assertNotNull(result);
        assertEquals("ENTITY_NOT_FOUND", result.title());
        assertEquals(404, result.statusCode());
        assertEquals("Company with id 123 was not found", result.detail());
        assertEquals("NOT_FOUND", result.errorCode());
        assertEquals("controller:CompanyController /api/companies/123", result.instance());
        assertEquals("corr-id-123", result.correlationId());
    }

    @Test
    void mapErrorLogEvent_withoutCorrelationIdInRequest_generatesCorrelationId() {
        ErrorLogEventDescriptor errorDescriptor = mock(ErrorLogEventDescriptor.class);
        when(errorDescriptor.getMessageKey()).thenReturn("VALIDATION_ERROR");
        when(errorDescriptor.getStatusCode()).thenReturn(400);
        when(errorDescriptor.getDetail()).thenReturn("Input validation failed");
        when(errorDescriptor.getErrorCode()).thenReturn("INVALID_INPUT");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/absences");

        ErrorLogEventDto result = ErrorLogEventFactory.mapErrorLogEvent(
                AbsenceController.class,
                errorDescriptor,
                request
        );

        assertNotNull(result);
        assertEquals("VALIDATION_ERROR", result.title());
        assertEquals(400, result.statusCode());
        assertEquals("Input validation failed", result.detail());
        assertEquals("INVALID_INPUT", result.errorCode());
        assertEquals("controller:AbsenceController /api/absences", result.instance());
        assertNotNull(result.correlationId());
    }

    @Test
    void mapErrorLogEvent_withNullControllerClass_usesUnknownController() {
        ErrorLogEventDescriptor errorDescriptor = mock(ErrorLogEventDescriptor.class);
        when(errorDescriptor.getMessageKey()).thenReturn("UNKNOWN_ERROR");
        when(errorDescriptor.getStatusCode()).thenReturn(500);
        when(errorDescriptor.getDetail()).thenReturn("An unexpected error occurred");
        when(errorDescriptor.getErrorCode()).thenReturn("INTERNAL_SERVER_ERROR");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/health");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-id-500");

        ErrorLogEventDto result = ErrorLogEventFactory.mapErrorLogEvent(
                null,
                errorDescriptor,
                request
        );

        assertNotNull(result);
        assertEquals("UNKNOWN_ERROR", result.title());
        assertEquals(500, result.statusCode());
        assertEquals("An unexpected error occurred", result.detail());
        assertEquals("INTERNAL_SERVER_ERROR", result.errorCode());
        assertEquals("controller:UnknownController /api/health", result.instance());
        assertEquals("corr-id-500", result.correlationId());
    }

    @Test
    void mapErrorLogEvent_withDifferentErrorTypes_mapsCorrectly() {
        ErrorLogEventDescriptor errorDescriptor = mock(ErrorLogEventDescriptor.class);
        when(errorDescriptor.getMessageKey()).thenReturn("DUPLICATE_ENTITY");
        when(errorDescriptor.getStatusCode()).thenReturn(409);
        when(errorDescriptor.getDetail()).thenReturn("Company with tax id already exists");
        when(errorDescriptor.getErrorCode()).thenReturn("ENTITY_EXISTS");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/companies");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-id-conflict");

        ErrorLogEventDto result = ErrorLogEventFactory.mapErrorLogEvent(
                CompanyController.class,
                errorDescriptor,
                request
        );

        assertNotNull(result);
        assertEquals("DUPLICATE_ENTITY", result.title());
        assertEquals(409, result.statusCode());
        assertEquals("Company with tax id already exists", result.detail());
        assertEquals("ENTITY_EXISTS", result.errorCode());
        assertEquals("controller:CompanyController /api/companies", result.instance());
        assertEquals("corr-id-conflict", result.correlationId());
    }

    // Mock controller classes for testing
    static class CompanyController {}
    static class AbsenceController {}
}
