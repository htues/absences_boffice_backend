package com.hftamayo.absencesbobe.shared.infrastructure.audit;

import com.hftamayo.absencesbobe.shared.web.dto.ApplicationLogEventDto;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
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

        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("timestamp", event.timestamp());
        arguments.put("severity", event.severity());
        arguments.put("eventType", event.eventType());
        arguments.put("eventCode", event.eventCode());
        arguments.put("message", event.message());
        arguments.put("statusCode", event.statusCode());
        arguments.put("correlationId", event.correlationId());
        arguments.put("path", event.path());
        arguments.put("httpMethod", event.httpMethod());
        arguments.put("source", event.source());
        arguments.put("detail", event.detail());
        arguments.put("traceId", event.traceId());
        arguments.put("context", event.context() == null ? Map.of() : event.context());

        return arguments;
    }
}