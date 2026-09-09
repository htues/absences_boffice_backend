package com.hftamayo.absencesbobe.shared.web.factory;

import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.dto.ApiResponseDto;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

@NoArgsConstructor
public final class ApiResponseFactory {

    /**
     * Main factory method: maps a Result from the application layer into a HTTP response
     * that matches the frontend contract.
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
            return success(successCode, result.value(), cache);
        }

        return error(resolveErrorResponse(result.error()), cache);
    }

    /**
     * Convenience: success response.
     */
    public static <T> ResponseEntity<ApiResponseDto<?>> success(
            SuccessApiResponse code,
            T data,
            Long cache
    ) {
        Objects.requireNonNull(code, "code must not be null");

        ApiResponseDto<T> body = ApiResponseDto.response(code, data, cache);
        return ResponseEntity.status(code.getStatusCode()).body(body);
    }

    /**
     * Convenience: error response.
     */
    public static ResponseEntity<ApiResponseDto<?>> error(
            ErrorApiResponse code,
            Long cache
    ) {
        ErrorApiResponse safeCode = code == null
                ? ErrorApiResponse.UNKNOWN_ERROR
                : code;

        ApiResponseDto<Void> body = ApiResponseDto.response(safeCode, null, cache);
        return ResponseEntity.status(safeCode.getStatusCode()).body(body);
    }

    /**
     * Convenience: unknown error response (500).
     */
    public static ResponseEntity<ApiResponseDto<?>> unknownError(Long cache) {
        return error(ErrorApiResponse.UNKNOWN_ERROR, cache);
    }

    private static ErrorApiResponse resolveErrorResponse(ApiResponseDescriptor descriptor) {
        return descriptor instanceof ErrorApiResponse errorCode
                ? errorCode
                : ErrorApiResponse.UNKNOWN_ERROR;
    }
}