package com.hftamayo.absencesbobe.shared.web.error;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.hftamayo.absencesbobe.shared.infrastructure.audit.ApplicationEventLogger;
import com.hftamayo.absencesbobe.shared.web.constants.CorrelationConstants;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class GlobalValidationExceptionHandlerTest {

    private ApplicationEventLogger eventLogger;
    private GlobalValidationExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        eventLogger = mock(ApplicationEventLogger.class);
        handler = new GlobalValidationExceptionHandler(eventLogger);

        request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "corr-test");
    }

    @Test
    void handleBodyValidationReturns422WithBodyErrors() {
        FieldError fieldError = new FieldError("request", "name", "Name is required");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(bindingResult.getFieldErrorCount()).thenReturn(1);

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiResponseDto<ValidationErrorResponseDto>> response =
                handler.handleBodyValidation(exception, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());

        ApiResponseDto<ValidationErrorResponseDto> body = response.getBody();
        assertValidationErrorResponse(body);

        ValidationErrorResponseDto data = body.getData();
        assertNotNull(data);
        assertEquals("Request body validation failed", data.reason());

        List<ValidationFieldErrorDto> errors = data.errors();
        assertEquals(1, errors.size());
        assertEquals("name", errors.getFirst().field());
        assertEquals("Name is required", errors.getFirst().message());

        verify(eventLogger).warn(any(ApplicationLogEventDto.class));
        verifyNoMoreInteractions(eventLogger);
    }

    @Test
    void handleConstraintViolationReturns422WithParameterErrors() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);

        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("companyId");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must be greater than 0");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ApiResponseDto<ValidationErrorResponseDto>> response =
                handler.handleConstraintViolation(exception, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());

        ApiResponseDto<ValidationErrorResponseDto> body = response.getBody();
        assertValidationErrorResponse(body);

        ValidationErrorResponseDto data = body.getData();
        assertNotNull(data);
        assertEquals("Request parameter validation failed", data.reason());

        List<ValidationFieldErrorDto> errors = data.errors();
        assertEquals(1, errors.size());
        assertEquals("companyId", errors.getFirst().field());
        assertEquals("must be greater than 0", errors.getFirst().message());

        verify(eventLogger).warn(any(ApplicationLogEventDto.class));
        verifyNoMoreInteractions(eventLogger);
    }

    @Test
    void handleUnreadableBodyReturnsUnknownFieldWhenJsonContainsUnexpectedProperty() {
        UnrecognizedPropertyException unknownProperty = mock(UnrecognizedPropertyException.class);
        when(unknownProperty.getPropertyName()).thenReturn("unknownField");

        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Malformed", unknownProperty);

        ResponseEntity<ApiResponseDto<MalformedRequestResponseDto>> response =
                handler.handleUnreadableBody(exception, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());

        ApiResponseDto<MalformedRequestResponseDto> body = response.getBody();
        assertValidationErrorResponse(body);

        MalformedRequestResponseDto data = body.getData();
        assertNotNull(data);
        assertEquals("Unknown JSON field", data.reason());
        assertEquals("unknownField", data.field());

        verify(eventLogger).warn(any(ApplicationLogEventDto.class));
        verifyNoMoreInteractions(eventLogger);
    }

    @Test
    void handleUnreadableBodyReturnsMalformedReasonForGenericPayloadErrors() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Malformed");

        ResponseEntity<ApiResponseDto<MalformedRequestResponseDto>> response =
                handler.handleUnreadableBody(exception, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());

        ApiResponseDto<MalformedRequestResponseDto> body = response.getBody();
        assertValidationErrorResponse(body);

        MalformedRequestResponseDto data = body.getData();
        assertNotNull(data);
        assertEquals("Malformed JSON request", data.reason());
        assertNull(data.field());

        verify(eventLogger).warn(any(ApplicationLogEventDto.class));
        verifyNoMoreInteractions(eventLogger);
    }

    @Test
    void handleUnknownExceptionReturns500UnknownErrorAndLogsException() {
        RuntimeException exception = new RuntimeException("boom");

        ResponseEntity<ApiResponseDto<?>> response =
                handler.handleUnknownException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ApiResponseDto<?> body = response.getBody();
        assertNotNull(body);
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getStatusCode(), body.getStatusCode());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getMessageKey(), body.getResultMessage());
        assertEquals(ErrorApiResponse.UNKNOWN_ERROR.getResponseType(), body.getResponseType());
        assertNull(body.getData());

        verify(eventLogger).error(any(ApplicationLogEventDto.class), same(exception));
        verifyNoMoreInteractions(eventLogger);
    }

    private void assertValidationErrorResponse(ApiResponseDto<?> body) {
        assertNotNull(body);
        assertEquals(ErrorApiResponse.VALIDATION_ERROR.getStatusCode(), body.getStatusCode());
        assertEquals(ErrorApiResponse.VALIDATION_ERROR.getMessageKey(), body.getResultMessage());
        assertEquals(ErrorApiResponse.VALIDATION_ERROR.getResponseType(), body.getResponseType());
    }
}