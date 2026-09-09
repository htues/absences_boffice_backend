package com.hftamayo.absencesbobe.shared.infrastructure.audit;

import com.hftamayo.absencesbobe.shared.web.dto.ApplicationLogEventDto;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ApplicationEventLogger {

    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_TRACE_ID = "traceId";
    private static final String MDC_EVENT_TYPE = "eventType";
    private static final String MDC_EVENT_CODE = "eventCode";

    public void info(ApplicationLogEventDto event) {
        withMdc(event, () -> log.info("application_event {}", toStructuredArguments(event)));
    }

    public void warn(ApplicationLogEventDto event) {
        withMdc(event, () -> log.warn("application_event {}", toStructuredArguments(event)));
    }

    public void error(ApplicationLogEventDto event) {
        withMdc(event, () -> log.error("application_event {}", toStructuredArguments(event)));
    }

    public void error(ApplicationLogEventDto event, Throwable throwable) {
        withMdc(event, () -> log.error("application_event {}", toStructuredArguments(event), throwable));
    }

    private void withMdc(ApplicationLogEventDto event, Runnable action) {
        if (event == null) {
            action.run();
            return;
        }

        putIfPresent(MDC_CORRELATION_ID, event.correlationId());
        putIfPresent(MDC_TRACE_ID, event.traceId());
        putIfPresent(MDC_EVENT_TYPE, event.eventType());
        putIfPresent(MDC_EVENT_CODE, event.eventCode());

        try {
            action.run();
        } finally {
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_TRACE_ID);
            MDC.remove(MDC_EVENT_TYPE);
            MDC.remove(MDC_EVENT_CODE);
        }
    }

    private void putIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }

    private Map<String, Object> toStructuredArguments(ApplicationLogEventDto event) {
        if (event == null) {
            return Map.of();
        }

        return Map.ofEntries(
                Map.entry("timestamp", event.timestamp()),
                Map.entry("severity", event.severity()),
                Map.entry("eventType", event.eventType()),
                Map.entry("eventCode", event.eventCode()),
                Map.entry("message", event.message()),
                Map.entry("statusCode", event.statusCode()),
                Map.entry("correlationId", event.correlationId()),
                Map.entry("path", event.path()),
                Map.entry("httpMethod", event.httpMethod()),
                Map.entry("source", event.source()),
                Map.entry("detail", event.detail()),
                Map.entry("traceId", event.traceId()),
                Map.entry("context", event.context() == null ? Map.of() : event.context())
        );
    }
}