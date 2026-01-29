package com.hftamayo.absencesbobe.shared.web.factory;

import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorCode;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessCode;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import com.hftamayo.absencesbobe.shared.web.dto.ErrorLogEventDto;
import com.hftamayo.absencesbobe.shared.web.error.ErrorLogEventDescriptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

public final class ApiResponseFactory {
    private ApiResponseFactory() {}

    /**
     * Main factory method: maps a Result from the application layer into a HTTP response
     * that matches the frontend contract (type/responseType, code/statusCode, resultMessage, data).
     */
    public static <T> ResponseEntity<ApiResponseDto<?>> fromResult(
            Result<T, ? extends ErrorLogEventDescriptor> result,
            SuccessCode successCode,
            Long cache
    ) {
        Objects.requireNonNull(successCode, "successCode must not be null");

        if (result == null) {
            return unknownError(cache);
        }

        if (result.isSuccess()) {
            ApiResponseDto<T> body = ApiResponseDto.response(successCode, result.value(), cache);
            return ResponseEntity.status(successCode.getStatusCode()).body(body);
        }

        ErrorCode errorCode = responseError(result.error());
        ApiResponseDto<Void> body = ApiResponseDto.response(errorCode, null, cache);
        return ResponseEntity.status(errorCode.getStatusCode()).body(body);
    }

    /**
     * Optional helper: builds the structured error event for logging/tracing.
     * Note: not returned to the frontend (since ApiResponseDto no longer includes an "error" field).
     */
    public static ErrorLogEventDto buildErrorEvent(
            Class<?> controllerClass,
            ErrorLogEventDescriptor error,
            HttpServletRequest request
    ) {
        return ErrorLogEventFactory.mapErrorLogEvent(controllerClass, error, request);
    }

    /**
     * Maps an ErrorLogEventDescriptor to the ErrorCode used in the response.
     */
    public static ErrorCode responseError(ErrorLogEventDescriptor error) {
        return (error == null || error.getType() == null)
                ? ErrorCode.UNKNOWN_ERROR
                : error.getType();
    }

    /**
     * Convenience: unknown error response (500).
     */
    public static ResponseEntity<ApiResponseDto<?>> unknownError(Long cache) {
        ErrorCode code = ErrorCode.UNKNOWN_ERROR;
        ApiResponseDto<Void> body = ApiResponseDto.response(code, null, cache);
        return ResponseEntity.status(code.getStatusCode()).body(body);
    }
}