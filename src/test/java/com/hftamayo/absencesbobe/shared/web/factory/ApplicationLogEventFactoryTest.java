package com.hftamayo.absencesbobe.shared.web.factory;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.CorrelationConstants;
import com.hftamayo.absencesbobe.shared.web.dto.ApplicationLogEventDto;
import com.hftamayo.absencesbobe.shared.web.error.ErrorLogEventDescriptor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationLogEventFactoryTest {

    @Test
    void fromBusinessException_withValidInputs_returnsBusinessErrorEvent() {
        ErrorLogEventDescriptor errorDescriptor = mock(ErrorLogEventDescriptor.class);
        when(errorDescriptor.getType()).thenReturn(ErrorApiResponse.NOT_FOUND);
        when(errorDescriptor.getDetail()).thenReturn("Company with id 123 was not found");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/companies/123");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-id-123");

        ApplicationLogEventDto result = ApplicationLogEventFactory.fromBusinessException(
                CompanyController.class,
                errorDescriptor,
                request
        );

        assertNotNull(result);
        assertNotNull(result.timestamp());
        assertEquals("WARN", result.severity());
        assertEquals("BUSINESS_ERROR", result.eventType());
        assertEquals("ENTITY_NOT_FOUND", result.eventCode());
        assertEquals("ENTITY_NOT_FOUND", result.message());
        assertEquals("Company with id 123 was not found", result.detail());
        assertEquals(404, result.statusCode());
        assertEquals("corr-id-123", result.correlationId());
        assertEquals("/api/companies/123", result.path());
        assertEquals("GET", result.httpMethod());
        assertEquals("CompanyController", result.source());
        assertNull(result.traceId());
        assertEquals("error", result.context().get("responseType"));
        assertEquals("NOT_FOUND", result.context().get("errorCode"));
    }

    @Test
    void fromBusinessException_withoutCorrelationIdInRequest_generatesCorrelationId() {
        ErrorLogEventDescriptor errorDescriptor = mock(ErrorLogEventDescriptor.class);
        when(errorDescriptor.getType()).thenReturn(ErrorApiResponse.VALIDATION_ERROR);
        when(errorDescriptor.getDetail()).thenReturn("Input validation failed");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/absences");

        ApplicationLogEventDto result = ApplicationLogEventFactory.fromBusinessException(
                AbsenceController.class,
                errorDescriptor,
                request
        );

        assertNotNull(result);
        assertEquals("WARN", result.severity());
        assertEquals("BUSINESS_ERROR", result.eventType());
        assertEquals("VALIDATION_ERROR", result.eventCode());
        assertEquals(422, result.statusCode());
        assertEquals("Input validation failed", result.detail());
        assertNotNull(result.correlationId());
        assertEquals("/api/absences", result.path());
        assertEquals("POST", result.httpMethod());
        assertEquals("AbsenceController", result.source());
    }

    @Test
    void fromBusinessException_withNullControllerClass_usesUnknownSource() {
        ErrorLogEventDescriptor errorDescriptor = mock(ErrorLogEventDescriptor.class);
        when(errorDescriptor.getType()).thenReturn(ErrorApiResponse.UNKNOWN_ERROR);
        when(errorDescriptor.getDetail()).thenReturn("An unexpected error occurred");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/health");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-id-500");

        ApplicationLogEventDto result = ApplicationLogEventFactory.fromBusinessException(
                null,
                errorDescriptor,
                request
        );

        assertNotNull(result);
        assertEquals("WARN", result.severity());
        assertEquals("BUSINESS_ERROR", result.eventType());
        assertEquals("UNKNOWN_ERROR", result.eventCode());
        assertEquals(500, result.statusCode());
        assertEquals("An unexpected error occurred", result.detail());
        assertEquals("corr-id-500", result.correlationId());
        assertEquals("/api/health", result.path());
        assertEquals("GET", result.httpMethod());
        assertEquals("UnknownSource", result.source());
    }

    @Test
    void fromBusinessException_withNullError_fallsBackToUnknownError() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("DELETE");
        request.setRequestURI("/api/companies/123");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-id-null-error");

        ApplicationLogEventDto result = ApplicationLogEventFactory.fromBusinessException(
                CompanyController.class,
                null,
                request
        );

        assertNotNull(result);
        assertEquals("WARN", result.severity());
        assertEquals("BUSINESS_ERROR", result.eventType());
        assertEquals("UNKNOWN_ERROR", result.eventCode());
        assertNull(result.detail());
        assertEquals(500, result.statusCode());
        assertEquals("corr-id-null-error", result.correlationId());
        assertEquals("/api/companies/123", result.path());
        assertEquals("DELETE", result.httpMethod());
        assertEquals("CompanyController", result.source());
        assertEquals("UNKNOWN_ERROR", result.context().get("errorCode"));
    }

    @Test
    void fromSuccess_withValidInputs_returnsSuccessEvent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/companies");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-id-created");

        ApplicationLogEventDto result = ApplicationLogEventFactory.fromSuccess(
                CompanyController.class,
                SuccessApiResponse.CREATED,
                request
        );

        assertNotNull(result);
        assertNotNull(result.timestamp());
        assertEquals("INFO", result.severity());
        assertEquals("API_SUCCESS", result.eventType());
        assertEquals("ENTITY_CREATED", result.eventCode());
        assertEquals("ENTITY_CREATED", result.message());
        assertEquals("API request completed successfully", result.detail());
        assertEquals(201, result.statusCode());
        assertEquals("corr-id-created", result.correlationId());
        assertEquals("/api/companies", result.path());
        assertEquals("POST", result.httpMethod());
        assertEquals("CompanyController", result.source());
        assertEquals("success", result.context().get("responseType"));
    }

    @Test
    void fromSuccess_withNullResponse_usesReadAsFallback() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/companies");

        ApplicationLogEventDto result = ApplicationLogEventFactory.fromSuccess(
                CompanyController.class,
                null,
                request
        );

        assertNotNull(result);
        assertEquals("INFO", result.severity());
        assertEquals("API_SUCCESS", result.eventType());
        assertEquals("ENTITY_RETRIEVED", result.eventCode());
        assertEquals(200, result.statusCode());
        assertEquals("/api/companies", result.path());
        assertEquals("GET", result.httpMethod());
    }

    @Test
    void fromUnknownException_withException_returnsUnknownErrorEvent() {
        RuntimeException exception = new RuntimeException("boom");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("PUT");
        request.setRequestURI("/api/companies/123");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-id-boom");

        ApplicationLogEventDto result = ApplicationLogEventFactory.fromUnknownException(
                CompanyController.class,
                exception,
                request
        );

        assertNotNull(result);
        assertNotNull(result.timestamp());
        assertEquals("ERROR", result.severity());
        assertEquals("UNKNOWN_ERROR", result.eventType());
        assertEquals("UNKNOWN_ERROR", result.eventCode());
        assertEquals("Unexpected application error", result.message());
        assertEquals("boom", result.detail());
        assertEquals(500, result.statusCode());
        assertEquals("corr-id-boom", result.correlationId());
        assertEquals("/api/companies/123", result.path());
        assertEquals("PUT", result.httpMethod());
        assertEquals("CompanyController", result.source());
        assertEquals(RuntimeException.class.getName(), result.context().get("exception"));
    }

    @Test
    void fromUnknownException_withNullRequest_keepsRequestFieldsNull() {
        RuntimeException exception = new RuntimeException("boom");

        ApplicationLogEventDto result = ApplicationLogEventFactory.fromUnknownException(
                CompanyController.class,
                exception,
                null
        );

        assertNotNull(result);
        assertEquals("ERROR", result.severity());
        assertEquals("UNKNOWN_ERROR", result.eventType());
        assertEquals("UNKNOWN_ERROR", result.eventCode());
        assertEquals(500, result.statusCode());
        assertNull(result.correlationId());
        assertNull(result.path());
        assertNull(result.httpMethod());
        assertEquals("CompanyController", result.source());
    }

    static class CompanyController {
    }

    static class AbsenceController {
    }
}