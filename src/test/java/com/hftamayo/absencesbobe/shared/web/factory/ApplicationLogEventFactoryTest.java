package com.hftamayo.absencesbobe.shared.web.factory;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.CorrelationConstants;
import com.hftamayo.absencesbobe.shared.web.dto.ApplicationLogEventDto;
import com.hftamayo.absencesbobe.shared.web.error.ErrorLogEventDescriptor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

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

    @Test
    void fromValidationException_returnsValidationErrorEvent() {
        FieldError fieldError = new FieldError("request", "name", "Name is required");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(bindingResult.getFieldErrorCount()).thenReturn(1);

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/companies");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-id-validation");

        ApplicationLogEventDto result = ApplicationLogEventFactory.fromValidationException(
                CompanyController.class,
                exception,
                request
        );

        assertNotNull(result);
        assertNotNull(result.timestamp());
        assertEquals("WARN", result.severity());
        assertEquals("VALIDATION_ERROR", result.eventType());
        assertEquals("VALIDATION_ERROR", result.eventCode());
        assertEquals("Request body validation failed", result.message());
        assertEquals("Request body validation failed", result.detail());
        assertEquals(422, result.statusCode());
        assertEquals("corr-id-validation", result.correlationId());
        assertEquals("/api/companies", result.path());
        assertEquals("POST", result.httpMethod());
        assertEquals("CompanyController", result.source());
        assertEquals("error", result.context().get("responseType"));
        assertEquals(MethodArgumentNotValidException.class.getSimpleName(), result.context().get("exception"));
        assertEquals(1, result.context().get("fieldErrorCount"));
    }

    @Test
    void fromConstraintViolationException_returnsValidationErrorEvent() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);

        ConstraintViolationException exception =
                new ConstraintViolationException("Parameter validation failed", Set.of(violation));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/companies");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-id-constraint");

        ApplicationLogEventDto result = ApplicationLogEventFactory.fromConstraintViolationException(
                CompanyController.class,
                exception,
                request
        );

        assertNotNull(result);
        assertNotNull(result.timestamp());
        assertEquals("WARN", result.severity());
        assertEquals("VALIDATION_ERROR", result.eventType());
        assertEquals("VALIDATION_ERROR", result.eventCode());
        assertEquals("Request parameter validation failed", result.message());
        assertEquals("Parameter validation failed", result.detail());
        assertEquals(422, result.statusCode());
        assertEquals("corr-id-constraint", result.correlationId());
        assertEquals("/api/companies", result.path());
        assertEquals("GET", result.httpMethod());
        assertEquals("CompanyController", result.source());
        assertEquals("error", result.context().get("responseType"));
        assertEquals(ConstraintViolationException.class.getSimpleName(), result.context().get("exception"));
        assertEquals(1, result.context().get("violationCount"));
    }

    @Test
    void fromUnreadableBodyException_returnsValidationErrorEvent() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Malformed JSON request");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/companies");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-id-malformed");

        ApplicationLogEventDto result = ApplicationLogEventFactory.fromUnreadableBodyException(
                CompanyController.class,
                exception,
                request
        );

        assertNotNull(result);
        assertNotNull(result.timestamp());
        assertEquals("WARN", result.severity());
        assertEquals("VALIDATION_ERROR", result.eventType());
        assertEquals("VALIDATION_ERROR", result.eventCode());
        assertEquals("Malformed JSON request", result.message());
        assertEquals("Malformed JSON request", result.detail());
        assertEquals(422, result.statusCode());
        assertEquals("corr-id-malformed", result.correlationId());
        assertEquals("/api/companies", result.path());
        assertEquals("POST", result.httpMethod());
        assertEquals("CompanyController", result.source());
        assertEquals("error", result.context().get("responseType"));
        assertEquals(HttpMessageNotReadableException.class.getSimpleName(), result.context().get("exception"));
    }

    @Test
    void fromUnknownException_withNullException_returnsUnknownErrorEventWithoutExceptionContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/companies");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-id-null-exception");

        ApplicationLogEventDto result = ApplicationLogEventFactory.fromUnknownException(
                CompanyController.class,
                null,
                request
        );

        assertNotNull(result);
        assertNotNull(result.timestamp());
        assertEquals("ERROR", result.severity());
        assertEquals("UNKNOWN_ERROR", result.eventType());
        assertEquals("UNKNOWN_ERROR", result.eventCode());
        assertEquals("Unexpected application error", result.message());
        assertNull(result.detail());
        assertEquals(500, result.statusCode());
        assertEquals("corr-id-null-exception", result.correlationId());
        assertEquals("/api/companies", result.path());
        assertEquals("GET", result.httpMethod());
        assertEquals("CompanyController", result.source());
        assertEquals("error", result.context().get("responseType"));
        assertFalse(result.context().containsKey("exception"));
    }

    @Test
    void fromBusinessException_whenDescriptorTypeIsNull_fallsBackToUnknownError() {
        ErrorLogEventDescriptor errorDescriptor = mock(ErrorLogEventDescriptor.class);
        when(errorDescriptor.getType()).thenReturn(null);
        when(errorDescriptor.getDetail()).thenReturn("Descriptor type is null");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("PATCH");
        request.setRequestURI("/api/companies/123");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-id-null-type");

        ApplicationLogEventDto result = ApplicationLogEventFactory.fromBusinessException(
                CompanyController.class,
                errorDescriptor,
                request
        );

        assertNotNull(result);
        assertNotNull(result.timestamp());
        assertEquals("WARN", result.severity());
        assertEquals("BUSINESS_ERROR", result.eventType());
        assertEquals("UNKNOWN_ERROR", result.eventCode());
        assertEquals("UNKNOWN_ERROR", result.message());
        assertEquals("Descriptor type is null", result.detail());
        assertEquals(500, result.statusCode());
        assertEquals("corr-id-null-type", result.correlationId());
        assertEquals("/api/companies/123", result.path());
        assertEquals("PATCH", result.httpMethod());
        assertEquals("CompanyController", result.source());
        assertEquals("error", result.context().get("responseType"));
        assertEquals("UNKNOWN_ERROR", result.context().get("errorCode"));
    }

    static class CompanyController {
    }

    static class AbsenceController {
    }
}