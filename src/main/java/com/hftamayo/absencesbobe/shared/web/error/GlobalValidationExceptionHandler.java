package com.hftamayo.absencesbobe.shared.web.error;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> handleBodyValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reason", "Request body validation failed");
        data.put("errors", errors);

        ApiResponseDto<Map<String, Object>> body =
                ApiResponseDto.response(ErrorApiResponse.VALIDATION_ERROR, data, null);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String, String>> errors = ex.getConstraintViolations()
                .stream()
                .map(this::toConstraintError)
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reason", "Request parameter validation failed");
        data.put("errors", errors);

        ApiResponseDto<Map<String, Object>> body =
                ApiResponseDto.response(ErrorApiResponse.VALIDATION_ERROR, data, null);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        Map<String, Object> data = new LinkedHashMap<>();

        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof UnrecognizedPropertyException unknownField) {
            data.put("reason", "Unknown JSON field");
            data.put("field", unknownField.getPropertyName());
        } else {
            data.put("reason", "Malformed JSON request");
        }

        ApiResponseDto<Map<String, Object>> body =
                ApiResponseDto.response(ErrorApiResponse.VALIDATION_ERROR, data, null);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    private Map<String, String> toFieldError(FieldError error) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("field", error.getField());
        result.put("message", error.getDefaultMessage());
        return result;
    }

    private Map<String, String> toConstraintError(ConstraintViolation<?> violation) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("field", violation.getPropertyPath().toString());
        result.put("message", violation.getMessage());
        return result;
    }
}