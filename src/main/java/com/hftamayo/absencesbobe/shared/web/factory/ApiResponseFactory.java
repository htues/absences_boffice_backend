package com.hftamayo.absencesbobe.shared.web.factory;

import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import com.hftamayo.absencesbobe.shared.web.dto.ErrorLogEventDto;
import com.hftamayo.absencesbobe.shared.web.error.ErrorLogEventDescriptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

@NoArgsConstructor
public final class ApiResponseFactory {

    /**
     * Main factory method: maps a Result from the application layer into a HTTP response
     * that matches the frontend contract (type/responseType, code/statusCode, resultMessage, data).
     */
    public static <T> ResponseEntity<ApiResponseDto<?>> fromResult(
            Result<T, ? extends ApiResponseDescriptor> result,
            SuccessApiResponse successCode,
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

        ApiResponseDescriptor err = result.error();
        ErrorApiResponse errorCode = (err instanceof ErrorApiResponse ec) ? ec : ErrorApiResponse.UNKNOWN_ERROR;

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
    public static ErrorApiResponse responseError(ErrorLogEventDescriptor error) {
        return (error == null || error.getType() == null)
                ? ErrorApiResponse.UNKNOWN_ERROR
                : error.getType();
    }

    /**
     * Convenience: unknown error response (500).
     */
    public static ResponseEntity<ApiResponseDto<?>> unknownError(Long cache) {
        ErrorApiResponse code = ErrorApiResponse.UNKNOWN_ERROR;
        ApiResponseDto<Void> body = ApiResponseDto.response(code, null, cache);
        return ResponseEntity.status(code.getStatusCode()).body(body);
    }
}