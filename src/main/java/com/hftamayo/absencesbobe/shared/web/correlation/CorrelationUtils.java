package com.hftamayo.absencesbobe.shared.web.correlation;

import com.hftamayo.absencesbobe.shared.web.constants.CorrelationConstants;
import jakarta.servlet.http.HttpServletRequest;

public final class CorrelationUtils {

    private CorrelationUtils() {}

    public static String getCorrelationId(HttpServletRequest request) {
        Object attr = request.getAttribute(CorrelationConstants.ATTRIBUTE);
        if (attr instanceof String s && !s.isBlank()) {
            return s;
        }
        String header = request.getHeader(CorrelationConstants.HEADER);
        return header != null ? header : null;
    }

    public static String getInstance(Class<?> controllerClass, HttpServletRequest request) {
        String controller = controllerClass != null ? controllerClass.getSimpleName() : "UnknownController";
        return "controller:" + controller + " " + request.getRequestURI();
    }
}

