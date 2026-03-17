package com.hftamayo.absencesbobe.shared.web.error;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalValidationExceptionHandlerTest {

    private static final String KEY_REASON = "reason";
    private static final String KEY_FIELD = "field";

    private GlobalValidationExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalValidationExceptionHandler();
    }

    @Test
    void handleBodyValidationReturns422WithBodyErrors() {
        FieldError fieldError = new FieldError("request", "name", "Name is required");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiResponseDto<Map<String, Object>>> response = handler.handleBodyValidation(exception);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        ApiResponseDto<Map<String, Object>> body = response.getBody();
        assertValidationErrorResponse(body);

        Map<String, Object> data = body.getData();
        assertEquals("Request body validation failed", data.get(KEY_REASON));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> errors = (List<Map<String, String>>) data.get("errors");
        assertEquals(1, errors.size());
        assertEquals("name", errors.get(0).get(KEY_FIELD));
        assertEquals("Name is required", errors.get(0).get("message"));
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

        ResponseEntity<ApiResponseDto<Map<String, Object>>> response = handler.handleConstraintViolation(exception);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        ApiResponseDto<Map<String, Object>> body = response.getBody();
        assertValidationErrorResponse(body);

        Map<String, Object> data = body.getData();
        assertEquals("Request parameter validation failed", data.get(KEY_REASON));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> errors = (List<Map<String, String>>) data.get("errors");
        assertEquals(1, errors.size());
        assertEquals("companyId", errors.get(0).get(KEY_FIELD));
        assertEquals("must be greater than 0", errors.get(0).get("message"));
    }

    @Test
    void handleUnreadableBodyReturnsUnknownFieldWhenJsonContainsUnexpectedProperty() {
        UnrecognizedPropertyException unknownProperty = mock(UnrecognizedPropertyException.class);
        when(unknownProperty.getPropertyName()).thenReturn("unknownField");

        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Malformed", unknownProperty);

        ResponseEntity<ApiResponseDto<Map<String, Object>>> response = handler.handleUnreadableBody(exception);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        ApiResponseDto<Map<String, Object>> body = response.getBody();
        assertValidationErrorResponse(body);

        Map<String, Object> data = body.getData();
        assertEquals("Unknown JSON field", data.get(KEY_REASON));
        assertEquals("unknownField", data.get(KEY_FIELD));
    }

    @Test
    void handleUnreadableBodyReturnsMalformedReasonForGenericPayloadErrors() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Malformed");

        ResponseEntity<ApiResponseDto<Map<String, Object>>> response = handler.handleUnreadableBody(exception);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        ApiResponseDto<Map<String, Object>> body = response.getBody();
        assertValidationErrorResponse(body);

        Map<String, Object> data = body.getData();
        assertEquals("Malformed JSON request", data.get(KEY_REASON));
        assertNull(data.get(KEY_FIELD));
    }

    private void assertValidationErrorResponse(ApiResponseDto<Map<String, Object>> body) {
        assertNotNull(body);
        assertEquals(ErrorApiResponse.VALIDATION_ERROR.getStatusCode(), body.getStatusCode());
        assertEquals(ErrorApiResponse.VALIDATION_ERROR.getMessageKey(), body.getResultMessage());
    }
}
