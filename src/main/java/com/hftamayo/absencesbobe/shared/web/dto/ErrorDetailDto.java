package com.hftamayo.absencesbobe.shared.web.dto;

import lombok.Builder;

@Builder
public record ErrorDetailDto(
        String title,
        int status,
        String detail,
        String errorCode,
        String instance,
        String correlationId
) {
}
