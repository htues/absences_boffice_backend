package com.hftamayo.absencesbobe.shared.web.dto;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorCode;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessCode;
import com.hftamayo.absencesbobe.shared.web.constants.CodeDescriptor;
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

    private final Instant timestamp;
    private final Long cacheTTL;

    /**
     * Single entry-point for both success and error responses.
     * Pass either SuccessCode or ErrorCode (both implement ApiResponseDescriptor).
     */
    public static <T> ApiResponseDto<T> response(
            CodeDescriptor code,
            T data,
            Long cache
    ) {
        return ApiResponseDto.<T>builder()
                .responseType(code.getResponseType())
                .statusCode(code.getStatusCode())
                .resultMessage(code.getMessageKey())
                .data(data)
                .timestamp(Instant.now())
                .cacheTTL(cache)
                .build();
    }

    public static <T> ApiResponseDto<T> ok(SuccessCode code, T data, Long cache) {
        return response(code, data, cache);
    }

    public static ApiResponseDto<Void> fail(ErrorCode code, Long cache) {
        return response(code, null, cache);
    }
}
