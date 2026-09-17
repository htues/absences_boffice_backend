package com.hftamayo.absencesbobe.shared.infrastructure.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hftamayo.absencesbobe.shared.web.dto.ApplicationLogEventDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles({"test", "json-logs"})
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(
        classes = ApplicationEventLogger.class,
        args = "--spring.profiles.active=test,json-logs",
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
    void configuredProfileEmitsJsonLogs(CapturedOutput output) throws Exception {
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

        String capturedLogs = output.getAll();

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
    }
}