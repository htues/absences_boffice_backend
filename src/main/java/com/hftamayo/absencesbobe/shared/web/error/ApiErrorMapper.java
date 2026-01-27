package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.correlation.CorrelationUtils;
import com.hftamayo.absencesbobe.shared.web.dto.ErrorDetailDto;
import jakarta.servlet.http.HttpServletRequest;

public final class ApiErrorMapper {

    private ApiErrorMapper() {}

    public static ErrorDetailDto mapBusinessError(Class<?> controllerClass, BusinessError error, HttpServletRequest request) {
        String instance = CorrelationUtils.getInstance(controllerClass, request);
        String correlationId = CorrelationUtils.getCorrelationId(request);

        return ErrorDetailDto.builder()
                .title(error.getTitle())
                .status(error.getStatus())
                .detail(error.getDetail())
                .errorCode(error.getErrorCode())
                .instance(instance)
                .correlationId(correlationId)
                .build();
    }
}

