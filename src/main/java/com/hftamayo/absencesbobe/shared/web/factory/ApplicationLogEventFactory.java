package com.hftamayo.absencesbobe.shared.web.factory;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.correlation.CorrelationUtils;
import com.hftamayo.absencesbobe.shared.web.dto.ApplicationLogEventDto;
import com.hftamayo.absencesbobe.shared.web.error.ErrorLogEventDescriptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@NoArgsConstructor
public final class ApplicationLogEventFactory {

    private static final String SEVERITY_INFO = "INFO";
    private static final String SEVERITY_WARN = "WARN";
    private static final String SEVERITY_ERROR = "ERROR";

    private static final String EVENT_TYPE_API_SUCCESS = "API_SUCCESS";
    private static final String EVENT_TYPE_VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String EVENT_TYPE_BUSINESS_ERROR = "BUSINESS_ERROR";
    private static final String EVENT_TYPE_UNKNOWN_ERROR = "UNKNOWN_ERROR";

    private static final String UNKNOWN_SOURCE = "UnknownSource";

    public static ApplicationLogEventDto fromSuccess(
            Class<?> sourceClass,
            SuccessApiResponse response,
            HttpServletRequest request
    ) {
        SuccessApiResponse safeResponse = response == null
                ? SuccessApiResponse.READ
                : response;

        return baseBuilder(sourceClass, request)
                .severity(SEVERITY_INFO)
                .eventType(EVENT_TYPE_API_SUCCESS)
                .eventCode(safeResponse.getMessageKey())
                .message(safeResponse.getMessageKey())
                .detail("API request completed successfully")
                .statusCode(safeResponse.getStatusCode())
                .context(Map.of(
                        "responseType", safeResponse.getResponseType()
                ))
                .build();
    }

    public static ApplicationLogEventDto fromValidationException(
            Class<?> sourceClass,
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("responseType", ErrorApiResponse.VALIDATION_ERROR.getResponseType());
        context.put("exception", exception.getClass().getSimpleName());
        context.put("fieldErrorCount", exception.getBindingResult().getFieldErrorCount());

        return baseBuilder(sourceClass, request)
                .severity(SEVERITY_WARN)
                .eventType(EVENT_TYPE_VALIDATION_ERROR)
                .eventCode(ErrorApiResponse.VALIDATION_ERROR.getMessageKey())
                .message("Request body validation failed")
                .detail(exception.getMessage())
                .statusCode(ErrorApiResponse.VALIDATION_ERROR.getStatusCode())
                .context(context)
                .build();
    }

    public static ApplicationLogEventDto fromBusinessException(
            Class<?> sourceClass,
            ErrorLogEventDescriptor error,
            HttpServletRequest request
    ) {
        ErrorApiResponse response = resolveErrorResponse(error);

        return baseBuilder(sourceClass, request)
                .severity(SEVERITY_WARN)
                .eventType(EVENT_TYPE_BUSINESS_ERROR)
                .eventCode(response.getMessageKey())
                .message(response.getMessageKey())
                .detail(error == null ? null : error.getDetail())
                .statusCode(response.getStatusCode())
                .context(Map.of(
                        "responseType", response.getResponseType(),
                        "errorCode", response.name()
                ))
                .build();
    }

    public static ApplicationLogEventDto fromConstraintViolationException(
            Class<?> sourceClass,
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("responseType", ErrorApiResponse.VALIDATION_ERROR.getResponseType());
        context.put("exception", exception.getClass().getSimpleName());
        context.put("violationCount", exception.getConstraintViolations().size());

        return baseBuilder(sourceClass, request)
                .severity(SEVERITY_WARN)
                .eventType(EVENT_TYPE_VALIDATION_ERROR)
                .eventCode(ErrorApiResponse.VALIDATION_ERROR.getMessageKey())
                .message("Request parameter validation failed")
                .detail(exception.getMessage())
                .statusCode(ErrorApiResponse.VALIDATION_ERROR.getStatusCode())
                .context(context)
                .build();
    }

    public static ApplicationLogEventDto fromUnreadableBodyException(
            Class<?> sourceClass,
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("responseType", ErrorApiResponse.VALIDATION_ERROR.getResponseType());
        context.put("exception", exception.getClass().getSimpleName());

        return baseBuilder(sourceClass, request)
                .severity(SEVERITY_WARN)
                .eventType(EVENT_TYPE_VALIDATION_ERROR)
                .eventCode(ErrorApiResponse.VALIDATION_ERROR.getMessageKey())
                .message("Malformed JSON request")
                .detail(exception.getMessage())
                .statusCode(ErrorApiResponse.VALIDATION_ERROR.getStatusCode())
                .context(context)
                .build();
    }

    public static ApplicationLogEventDto fromUnknownException(
            Class<?> sourceClass,
            Exception exception,
            HttpServletRequest request
    ) {
        ErrorApiResponse response = ErrorApiResponse.UNKNOWN_ERROR;

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("responseType", response.getResponseType());

        if (exception != null) {
            context.put("exception", exception.getClass().getName());
        }

        return baseBuilder(sourceClass, request)
                .severity(SEVERITY_ERROR)
                .eventType(EVENT_TYPE_UNKNOWN_ERROR)
                .eventCode(response.getMessageKey())
                .message("Unexpected application error")
                .detail(exception == null ? null : exception.getMessage())
                .statusCode(response.getStatusCode())
                .context(context)
                .build();
    }

    private static ApplicationLogEventDto.ApplicationLogEventDtoBuilder baseBuilder(
            Class<?> sourceClass,
            HttpServletRequest request
    ) {
        return ApplicationLogEventDto.builder()
                .timestamp(Instant.now())
                .correlationId(resolveCorrelationId(request))
                .path(resolvePath(request))
                .httpMethod(resolveHttpMethod(request))
                .source(resolveSource(sourceClass))
                .traceId(null);
    }

    private static ErrorApiResponse resolveErrorResponse(ErrorLogEventDescriptor error) {
        return error == null || error.getType() == null
                ? ErrorApiResponse.UNKNOWN_ERROR
                : error.getType();
    }

    private static String resolveCorrelationId(HttpServletRequest request) {
        return request == null ? null : CorrelationUtils.getCorrelationId(request);
    }

    private static String resolvePath(HttpServletRequest request) {
        return request == null ? null : request.getRequestURI();
    }

    private static String resolveHttpMethod(HttpServletRequest request) {
        return request == null ? null : request.getMethod();
    }

    private static String resolveSource(Class<?> sourceClass) {
        return sourceClass == null ? UNKNOWN_SOURCE : sourceClass.getSimpleName();
    }
}