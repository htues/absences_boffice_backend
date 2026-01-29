package com.hftamayo.absencesbobe.shared.web.factory;

import com.hftamayo.absencesbobe.shared.web.correlation.CorrelationUtils;
import com.hftamayo.absencesbobe.shared.web.dto.ErrorLogEventDto;
import com.hftamayo.absencesbobe.shared.web.error.ErrorLogEventDescriptor;
import jakarta.servlet.http.HttpServletRequest;

public final class ErrorLogEventFactory {

    private ErrorLogEventFactory() {
    }

    public static ErrorLogEventDto mapErrorLogEvent(Class<?> controllerClass,
                                                    ErrorLogEventDescriptor error,
                                                    HttpServletRequest request) {
        String instance = CorrelationUtils.getInstance(controllerClass, request);
        String correlationId = CorrelationUtils.getCorrelationId(request);

        return ErrorLogEventDto.builder()
                .title(error.getMessageKey())
                .statusCode(error.getStatusCode())
                .detail(error.getDetail())
                .errorCode(error.getErrorCode())
                .instance(instance)
                .correlationId(correlationId)
                .build();
    }
}

