package com.hftamayo.absencesbobe.shared.web.dto;

import lombok.Builder;

@Builder
public record ApiErrorDto(
        String title,
        int statusCode,
        String detail,
        String errorCode,
        String instance,
        String correlationId
) {
}
