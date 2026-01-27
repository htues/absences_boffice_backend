package com.hftamayo.absencesbobe.shared.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class ApiResultDto<T> {
    private final NotificationType type;
    private final int code;
    private final String resultMessage;

    private final T data;

    private final ErrorDetailDto error;

    private final Instant timestamp;
    private final Long cacheTTL;

    public static <T> ApiResultDto<T> success(int code, T data, String message, Long cache) {
        return ApiResultDto.<T>builder()
                .type(NotificationType.success)
                .code(code)
                .resultMessage(message)
                .data(data)
                .error(null)
                .timestamp(Instant.now())
                .cacheTTL(cache)
                .build();
    }

    public static ApiResultDto<Void> error(int code, String message, ErrorDetailDto error, Long cache) {
        return ApiResultDto.<Void>builder()
                .type(NotificationType.error)
                .code(code)
                .resultMessage(message)
                .data(null)
                .error(error)
                .timestamp(Instant.now())
                .cacheTTL(cache)
                .build();
    }
}