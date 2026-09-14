package com.hftamayo.absencesbobe.shared.infrastructure.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.hftamayo.absencesbobe.shared.web.dto.ApplicationLogEventDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationEventLoggerTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(ApplicationEventLogger.class);
    private ListAppender<ILoggingEvent> appender;

    @AfterEach
    void tearDown() {
        if (appender != null) {
            logger.detachAppender(appender);
            appender.stop();
        }

        MDC.clear();
    }

    @Test
    void warnLogsApplicationEventWithMdcAndCleansMdcAfterwards() {
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        ApplicationEventLogger applicationEventLogger = new ApplicationEventLogger();

        ApplicationLogEventDto event = ApplicationLogEventDto.builder()
                .timestamp(Instant.parse("2026-09-14T10:15:30Z"))
                .severity("WARN")
                .eventType("VALIDATION_ERROR")
                .eventCode("validation.error")
                .message("Request body validation failed")
                .statusCode(422)
                .correlationId("corr-test")
                .path("/api/test")
                .httpMethod("POST")
                .source("GlobalValidationExceptionHandler")
                .detail("Request body validation failed")
                .traceId("trace-test")
                .context(Map.of("fieldErrorCount", 1))
                .build();

        applicationEventLogger.warn(event);

        assertEquals(1, appender.list.size());

        ILoggingEvent loggingEvent = appender.list.getFirst();

        assertEquals(Level.WARN, loggingEvent.getLevel());
        assertEquals("application_event {}", loggingEvent.getMessage());

        Map<String, String> mdc = loggingEvent.getMDCPropertyMap();
        assertEquals("corr-test", mdc.get("correlationId"));
        assertEquals("trace-test", mdc.get("traceId"));
        assertEquals("VALIDATION_ERROR", mdc.get("eventType"));
        assertEquals("validation.error", mdc.get("eventCode"));

        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("traceId"));
        assertNull(MDC.get("eventType"));
        assertNull(MDC.get("eventCode"));
    }

    @Test
    void errorLogsThrowable() {
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        ApplicationEventLogger applicationEventLogger = new ApplicationEventLogger();

        ApplicationLogEventDto event = ApplicationLogEventDto.builder()
                .timestamp(Instant.now())
                .severity("ERROR")
                .eventType("UNKNOWN_ERROR")
                .eventCode("unknown.error")
                .message("Unexpected application error")
                .statusCode(500)
                .correlationId("corr-test")
                .path("/api/test")
                .httpMethod("GET")
                .source("GlobalValidationExceptionHandler")
                .detail("boom")
                .traceId(null)
                .context(Map.of())
                .build();

        RuntimeException exception = new RuntimeException("boom");

        applicationEventLogger.error(event, exception);

        assertEquals(1, appender.list.size());

        ILoggingEvent loggingEvent = appender.list.getFirst();

        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals("application_event {}", loggingEvent.getMessage());
        assertNotNull(loggingEvent.getThrowableProxy());
        assertEquals(RuntimeException.class.getName(), loggingEvent.getThrowableProxy().getClassName());
        assertEquals("boom", loggingEvent.getThrowableProxy().getMessage());
    }

    @Test
    void infoWithNullEventLogsEmptyStructuredArguments() {
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        ApplicationEventLogger applicationEventLogger = new ApplicationEventLogger();

        applicationEventLogger.info(null);

        assertEquals(1, appender.list.size());

        ILoggingEvent loggingEvent = appender.list.getFirst();

        assertEquals(Level.INFO, loggingEvent.getLevel());
        assertEquals("application_event {}", loggingEvent.getMessage());
        assertTrue(loggingEvent.getMDCPropertyMap().isEmpty());
    }
}