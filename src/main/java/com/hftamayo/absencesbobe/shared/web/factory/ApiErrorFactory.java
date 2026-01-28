package com.hftamayo.absencesbobe.shared.web.factory;

import com.hftamayo.absencesbobe.shared.web.correlation.CorrelationUtils;
import com.hftamayo.absencesbobe.shared.web.dto.ApiErrorDto;
import com.hftamayo.absencesbobe.shared.web.error.ApiErrorDescriptor;
import jakarta.servlet.http.HttpServletRequest;

public final class ApiErrorFactory {

    private ApiErrorFactory() {
    }

    public static ApiErrorDto mapErrorLogEvent(Class<?> controllerClass,
                                               ApiErrorDescriptor error,
                                               HttpServletRequest request) {
        String instance = CorrelationUtils.getInstance(controllerClass, request);
        String correlationId = CorrelationUtils.getCorrelationId(request);

        return ApiErrorDto.builder()
                .title(error.getTitle())
                .statusCode(error.getStatusCode())
                .detail(error.getDetail())
                .errorCode(error.getErrorCode())
                .instance(instance)
                .correlationId(correlationId)
                .build();
    }
}

