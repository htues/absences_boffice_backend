package com.hftamayo.absencesbobe.shared.web.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
public record ApplicationLogEventDto(
        Instant timestamp,
        String severity,
        String eventType,
        String eventCode,
        String message,
        String detail,
        Integer statusCode,
        String correlationId,
        String traceId,
        String path,
        String httpMethod,
        String source,
        Map<String, Object> context
) {
}
