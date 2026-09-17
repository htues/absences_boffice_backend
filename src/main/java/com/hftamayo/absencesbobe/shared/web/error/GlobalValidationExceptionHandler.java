package com.hftamayo.absencesbobe.shared.web.error;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.hftamayo.absencesbobe.shared.infrastructure.audit.ApplicationEventLogger;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.*;
import com.hftamayo.absencesbobe.shared.web.factory.ApiResponseFactory;
import com.hftamayo.absencesbobe.shared.web.factory.ApplicationLogEventFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalValidationExceptionHandler {

    private final ApplicationEventLogger eventLogger;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<ValidationErrorResponseDto>> handleBodyValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ValidationFieldErrorDto> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        ValidationErrorResponseDto data = ValidationErrorResponseDto.builder()
                .reason("Request body validation failed")
                .errors(errors)
                .build();

        logValidationWarning(ApplicationLogEventFactory.fromValidationException(
                GlobalValidationExceptionHandler.class,
                ex,
                request
        ));

        return validationResponse(data);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDto<ValidationErrorResponseDto>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ValidationFieldErrorDto> errors = ex.getConstraintViolations()
                .stream()
                .map(this::toConstraintError)
                .toList();

        ValidationErrorResponseDto data = ValidationErrorResponseDto.builder()
                .reason("Request parameter validation failed")
                .errors(errors)
                .build();

        logValidationWarning(ApplicationLogEventFactory.fromConstraintViolationException(
                GlobalValidationExceptionHandler.class,
                ex,
                request
        ));

        return validationResponse(data);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<MalformedRequestResponseDto>> handleUnreadableBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        MalformedRequestResponseDto data = malformedRequestData(ex);

        logValidationWarning(ApplicationLogEventFactory.fromUnreadableBodyException(
                GlobalValidationExceptionHandler.class,
                ex,
                request
        ));

        ApiResponseDto<MalformedRequestResponseDto> body =
                ApiResponseDto.response(ErrorApiResponse.VALIDATION_ERROR, data, null);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<?>> handleUnknownException(
            Exception ex,
            HttpServletRequest request
    ) {
        ApplicationLogEventDto event = ApplicationLogEventFactory.fromUnknownException(
                GlobalValidationExceptionHandler.class,
                ex,
                request
        );

        eventLogger.error(event, ex);

        return ApiResponseFactory.unknownError(null);
    }

    private ResponseEntity<ApiResponseDto<ValidationErrorResponseDto>> validationResponse(
            ValidationErrorResponseDto data
    ) {
        ApiResponseDto<ValidationErrorResponseDto> body =
                ApiResponseDto.response(ErrorApiResponse.VALIDATION_ERROR, data, null);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    private void logValidationWarning(ApplicationLogEventDto event) {
        eventLogger.warn(event);
    }

    private MalformedRequestResponseDto malformedRequestData(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof UnrecognizedPropertyException unknownField) {
            return MalformedRequestResponseDto.builder()
                    .reason("Unknown JSON field")
                    .field(unknownField.getPropertyName())
                    .build();
        }

        return MalformedRequestResponseDto.builder()
                .reason("Malformed JSON request")
                .field(null)
                .build();
    }

    private ValidationFieldErrorDto toFieldError(FieldError error) {
        return ValidationFieldErrorDto.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .build();
    }

    private ValidationFieldErrorDto toConstraintError(ConstraintViolation<?> violation) {
        return ValidationFieldErrorDto.builder()
                .field(violation.getPropertyPath().toString())
                .message(violation.getMessage())
                .build();
    }
}