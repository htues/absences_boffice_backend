package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.infrastructure.audit.ApplicationEventLogger;
import com.hftamayo.absencesbobe.shared.web.dto.ApplicationLogEventDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.ElementType;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalValidationExceptionHandlerMvcTest.TestController.class)
@Import(GlobalValidationExceptionHandler.class)
class GlobalValidationExceptionHandlerMvcTest {

    @MockBean
    private ApplicationEventLogger eventLogger;

    private final MockMvc mockMvc;

    GlobalValidationExceptionHandlerMvcTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void invalidRequestBodyReturns422ValidationResponseAndLogsWarnEvent() throws Exception {
        mockMvc.perform(post("/test/body-validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.statusCode").value(422))
                .andExpect(jsonPath("$.resultMessage").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.responseType").value("error"))
                .andExpect(jsonPath("$.data.reason").value("Request body validation failed"))
                .andExpect(jsonPath("$.data.errors[0].field").value("name"))
                .andExpect(jsonPath("$.data.errors[0].message").value("Name is required"));

        ArgumentCaptor<ApplicationLogEventDto> eventCaptor =
                ArgumentCaptor.forClass(ApplicationLogEventDto.class);

        verify(eventLogger).warn(eventCaptor.capture());
        verifyNoMoreInteractions(eventLogger);

        ApplicationLogEventDto event = eventCaptor.getValue();

        assertEquals("WARN", event.severity());
        assertEquals("VALIDATION_ERROR", event.eventType());
        assertEquals("VALIDATION_ERROR", event.eventCode());
        assertEquals("Request body validation failed", event.message());
        assertEquals("Request body validation failed", event.detail());
        assertEquals(422, event.statusCode());
        assertEquals("/test/body-validation", event.path());
        assertEquals("POST", event.httpMethod());
        assertEquals("GlobalValidationExceptionHandler", event.source());
        assertEquals("error", event.context().get("responseType"));
        assertEquals(1, event.context().get("fieldErrorCount"));
    }

    @Test
    void constraintViolationReturns422ValidationResponseAndLogsWarnEvent() throws Exception {
        mockMvc.perform(get("/test/constraint-violation"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.statusCode").value(422))
                .andExpect(jsonPath("$.resultMessage").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.responseType").value("error"))
                .andExpect(jsonPath("$.data.reason").value("Request parameter validation failed"))
                .andExpect(jsonPath("$.data.errors[0].field").value("companyId"))
                .andExpect(jsonPath("$.data.errors[0].message").value("must be greater than 0"));

        ArgumentCaptor<ApplicationLogEventDto> eventCaptor =
                ArgumentCaptor.forClass(ApplicationLogEventDto.class);

        verify(eventLogger).warn(eventCaptor.capture());
        verifyNoMoreInteractions(eventLogger);

        ApplicationLogEventDto event = eventCaptor.getValue();

        assertEquals("WARN", event.severity());
        assertEquals("VALIDATION_ERROR", event.eventType());
        assertEquals("VALIDATION_ERROR", event.eventCode());
        assertEquals("Request parameter validation failed", event.message());
        assertEquals(422, event.statusCode());
        assertEquals("/test/constraint-violation", event.path());
        assertEquals("GET", event.httpMethod());
        assertEquals("GlobalValidationExceptionHandler", event.source());
        assertEquals("error", event.context().get("responseType"));
        assertEquals(1, event.context().get("violationCount"));
    }

    @Test
    void malformedJsonReturns422MalformedRequestResponseAndLogsWarnEvent() throws Exception {
        mockMvc.perform(post("/test/body-validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.statusCode").value(422))
                .andExpect(jsonPath("$.resultMessage").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.responseType").value("error"))
                .andExpect(jsonPath("$.data.reason").value("Malformed JSON request"))
                .andExpect(jsonPath("$.data.field").doesNotExist());

        ArgumentCaptor<ApplicationLogEventDto> eventCaptor =
                ArgumentCaptor.forClass(ApplicationLogEventDto.class);

        verify(eventLogger).warn(eventCaptor.capture());
        verifyNoMoreInteractions(eventLogger);

        ApplicationLogEventDto event = eventCaptor.getValue();

        assertEquals("WARN", event.severity());
        assertEquals("VALIDATION_ERROR", event.eventType());
        assertEquals("VALIDATION_ERROR", event.eventCode());
        assertEquals("Malformed JSON request", event.message());
        assertEquals(422, event.statusCode());
        assertEquals("/test/body-validation", event.path());
        assertEquals("POST", event.httpMethod());
        assertEquals("GlobalValidationExceptionHandler", event.source());
        assertEquals("error", event.context().get("responseType"));
    }

    @Test
    void unknownExceptionReturns500UnknownErrorResponseAndLogsErrorEvent() throws Exception {
        mockMvc.perform(get("/test/unknown-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.resultMessage").value("UNKNOWN_ERROR"))
                .andExpect(jsonPath("$.responseType").value("error"))
                .andExpect(jsonPath("$.data").doesNotExist());

        ArgumentCaptor<ApplicationLogEventDto> eventCaptor =
                ArgumentCaptor.forClass(ApplicationLogEventDto.class);

        ArgumentCaptor<Throwable> throwableCaptor =
                ArgumentCaptor.forClass(Throwable.class);

        verify(eventLogger).error(eventCaptor.capture(), throwableCaptor.capture());
        verifyNoMoreInteractions(eventLogger);

        ApplicationLogEventDto event = eventCaptor.getValue();
        Throwable throwable = throwableCaptor.getValue();

        assertEquals("ERROR", event.severity());
        assertEquals("UNKNOWN_ERROR", event.eventType());
        assertEquals("UNKNOWN_ERROR", event.eventCode());
        assertEquals("Unexpected application error", event.message());
        assertEquals("boom", event.detail());
        assertEquals(500, event.statusCode());
        assertEquals("/test/unknown-exception", event.path());
        assertEquals("GET", event.httpMethod());
        assertEquals("GlobalValidationExceptionHandler", event.source());
        assertEquals("error", event.context().get("responseType"));

        assertInstanceOf(RuntimeException.class, throwable);
        assertEquals("boom", throwable.getMessage());
    }

    @Controller
    @ResponseBody
    static class TestController {

        @PostMapping("/test/body-validation")
        void bodyValidation(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test/constraint-violation")
        void constraintViolation() {
            throw new ConstraintViolationException(Set.of(
                    new TestConstraintViolation("companyId", "must be greater than 0")
            ));
        }

        @GetMapping("/test/unknown-exception")
        void unknownException() {
            throw new RuntimeException("boom");
        }
    }

    record TestRequest(
            @NotBlank(message = "Name is required")
            String name
    ) {
    }

    record TestConstraintViolation(
            String propertyPath,
            String message
    ) implements ConstraintViolation<Object> {

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getMessageTemplate() {
            return message;
        }

        @Override
        public Object getRootBean() {
            return null;
        }

        @Override
        public Class<Object> getRootBeanClass() {
            return Object.class;
        }

        @Override
        public Object getLeafBean() {
            return null;
        }

        @Override
        public Object[] getExecutableParameters() {
            return new Object[0];
        }

        @Override
        public Object getExecutableReturnValue() {
            return null;
        }

        @Override
        public Path getPropertyPath() {
            return new Path() {
                @Override
                public java.util.Iterator<Node> iterator() {
                    return java.util.Collections.emptyIterator();
                }

                @Override
                public String toString() {
                    return propertyPath;
                }
            };
        }

        @Override
        public Object getInvalidValue() {
            return null;
        }

        @Override
        public jakarta.validation.metadata.ConstraintDescriptor<?> getConstraintDescriptor() {
            return null;
        }

        @Override
        public <U> U unwrap(Class<U> type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return propertyPath;
        }
    }
}