package com.hftamayo.absencesbobe.shared.web.dto;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class ApiResponseDto<T> {
    private final String responseType;
    private final int statusCode;
    private final String resultMessage;

    private final T data;
    private final PaginationDto pagination;

    private final Instant timestamp;
    private final Long cacheTTL;

    /**
     * Single entry-point for both success and error responses.
     * Pass either SuccessCode or ErrorCode (both implement ApiResponseDescriptor).
     */

    public static <T> ApiResponseDto<T> response(
            ApiResponseDescriptor code,
            T data,
            Long cache
    ) {
        return response(code, data, null, cache);
    }

    public static <T> ApiResponseDto<T> response(
            ApiResponseDescriptor code,
            T data,
            PaginationDto pagination,
            Long cache
    ) {
        return ApiResponseDto.<T>builder()
                .responseType(code.getResponseType())
                .statusCode(code.getStatusCode())
                .resultMessage(code.getMessageKey())
                .data(data)
                .pagination(pagination)
                .timestamp(Instant.now())
                .cacheTTL(cache)
                .build();
    }

    public static <T> ApiResponseDto<T> ok(SuccessApiResponse code, T data, Long cache) {
        return response(code, data, cache);
    }

    public static <T> ApiResponseDto<T> ok(
            SuccessApiResponse code,
            T data,
            com.hftamayo.absencesbobe.shared.web.dto.PaginationDto pagination,
            Long cache
    ) {
        return response(code, data, pagination, cache);
    }

    public static ApiResponseDto<Void> fail(
            ErrorApiResponse code, Long cache) {
        return response(code, null, cache);
    }
}
