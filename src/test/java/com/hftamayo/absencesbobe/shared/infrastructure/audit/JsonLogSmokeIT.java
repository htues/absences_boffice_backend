package com.hftamayo.absencesbobe.shared.infrastructure.audit;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hftamayo.absencesbobe.shared.web.dto.ApplicationLogEventDto;
import net.logstash.logback.composite.loggingevent.ArgumentsJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventFormattedTimestampJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventJsonProviders;
import net.logstash.logback.composite.loggingevent.LogLevelJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggerNameJsonProvider;
import net.logstash.logback.composite.loggingevent.MdcJsonProvider;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;
import net.logstash.logback.composite.loggingevent.StackTraceJsonProvider;
import net.logstash.logback.composite.loggingevent.ThreadNameJsonProvider;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import net.logstash.logback.composite.loggingevent.LoggingEventPatternJsonProvider;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@SpringBootTest(
        classes = ApplicationEventLogger.class,
        properties = {
                "spring.application.name=absences-backoffice",
                "app.environment=test"
        }
)
class JsonLogSmokeIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ApplicationEventLogger applicationEventLogger;

    @Test
    void configuredProfileEmitsJsonLogs() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(ApplicationEventLogger.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        LoggingEventCompositeJsonEncoder encoder = jsonEncoder(logger);
        OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
        appender.setName("JSON_LOG_SMOKE_TEST_APPENDER");
        appender.setContext(logger.getLoggerContext());
        appender.setEncoder(encoder);
        appender.setOutputStream(outputStream);

        encoder.start();
        appender.start();
        logger.addAppender(appender);

        try {
            ApplicationLogEventDto event = ApplicationLogEventDto.builder()
                    .timestamp(Instant.parse("2026-09-14T10:15:30Z"))
                    .severity("INFO")
                    .eventType("JSON_LOG_SMOKE_TEST")
                    .eventCode("JSON_LOG_SMOKE_TEST")
                    .message("JSON log smoke test")
                    .statusCode(200)
                    .correlationId("corr-json-smoke-test")
                    .path("/test/json-logs")
                    .httpMethod("GET")
                    .source("JsonLogSmokeIT")
                    .detail("Verifying JSON log pipeline")
                    .traceId("trace-json-smoke-test")
                    .context(Map.of("smokeTest", true))
                    .build();

            applicationEventLogger.info(event);

            appender.stop();

            String capturedLogs = outputStream.toString(StandardCharsets.UTF_8);

            String jsonLogLine = Arrays.stream(capturedLogs.split("\\R"))
                    .filter(line -> line.trim().startsWith("{"))
                    .filter(line -> line.contains("JSON log smoke test"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(
                            "JSON log line was not emitted. Captured output was:%n%s".formatted(capturedLogs)
                    ));

            JsonNode json = objectMapper.readTree(jsonLogLine);

            assertTrue(json.hasNonNull("timestamp"));
            assertEquals("INFO", json.get("level").asText());
            assertEquals(ApplicationEventLogger.class.getName(), json.get("logger").asText());
            assertTrue(json.hasNonNull("message"));
            assertTrue(json.has("mdc"));
            assertTrue(json.has("arguments"));
            assertEquals("absences-backoffice", json.get("service").asText());
            assertEquals("test", json.get("environment").asText());

            JsonNode mdc = json.get("mdc");
            assertEquals("corr-json-smoke-test", mdc.get("correlationId").asText());
            assertEquals("trace-json-smoke-test", mdc.get("traceId").asText());
            assertEquals("JSON_LOG_SMOKE_TEST", mdc.get("eventType").asText());
            assertEquals("JSON_LOG_SMOKE_TEST", mdc.get("eventCode").asText());
        } finally {
            logger.detachAppender(appender);
            appender.stop();
            encoder.stop();
        }
    }

    private LoggingEventCompositeJsonEncoder jsonEncoder(Logger logger) {
        LoggingEventJsonProviders providers = new LoggingEventJsonProviders();

        LoggingEventFormattedTimestampJsonProvider timestampProvider = new LoggingEventFormattedTimestampJsonProvider();
        timestampProvider.setFieldName("timestamp");
        providers.addTimestamp(timestampProvider);

        LogLevelJsonProvider logLevelProvider = new LogLevelJsonProvider();
        logLevelProvider.setFieldName("level");
        providers.addLogLevel(logLevelProvider);

        LoggerNameJsonProvider loggerNameProvider = new LoggerNameJsonProvider();
        loggerNameProvider.setFieldName("logger");
        providers.addLoggerName(loggerNameProvider);

        ThreadNameJsonProvider threadNameProvider = new ThreadNameJsonProvider();
        threadNameProvider.setFieldName("thread");
        providers.addThreadName(threadNameProvider);

        MessageJsonProvider messageProvider = new MessageJsonProvider();
        messageProvider.setFieldName("message");
        providers.addMessage(messageProvider);

        MdcJsonProvider mdcProvider = new MdcJsonProvider();
        mdcProvider.setFieldName("mdc");
        providers.addMdc(mdcProvider);

        ArgumentsJsonProvider argumentsProvider = new ArgumentsJsonProvider();
        argumentsProvider.setFieldName("arguments");
        providers.addArguments(argumentsProvider);

        StackTraceJsonProvider stackTraceProvider = new StackTraceJsonProvider();
        stackTraceProvider.setFieldName("stackTrace");
        providers.addStackTrace(stackTraceProvider);

        LoggingEventPatternJsonProvider patternProvider = new LoggingEventPatternJsonProvider();
        patternProvider.setPattern("""
                {
                  "service": "absences-backoffice",
                  "environment": "test"
                }
                """);
        providers.addProvider(patternProvider);

        LoggingEventCompositeJsonEncoder encoder = new LoggingEventCompositeJsonEncoder();
        encoder.setContext(logger.getLoggerContext());
        encoder.setProviders(providers);

        return encoder;
    }
}