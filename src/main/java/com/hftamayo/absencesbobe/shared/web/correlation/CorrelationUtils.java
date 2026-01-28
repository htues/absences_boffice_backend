package com.hftamayo.absencesbobe.shared.web.correlation;

import com.hftamayo.absencesbobe.shared.web.constants.CorrelationConstants;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public final class CorrelationUtils {

    private CorrelationUtils() {}

    public static String getCorrelationId(HttpServletRequest request) {
        Object attr = request.getAttribute(CorrelationConstants.ATTRIBUTE);
        if (attr instanceof String s && !s.isBlank()) {
            return s;
        }

        // Fallback: if filter didn't run (tests/misconfig), recover from header or generate.
        String header = request.getHeader(CorrelationConstants.HEADER);
        if (header != null && !header.isBlank()) {
            String trimmed = header.trim();
            request.setAttribute(CorrelationConstants.ATTRIBUTE, trimmed);
            return trimmed;
        }

        String generated = UUID.randomUUID().toString();
        request.setAttribute(CorrelationConstants.ATTRIBUTE, generated);
        return generated;
    }

    public static String getInstance(Class<?> controllerClass, HttpServletRequest request) {
        String controller = controllerClass != null ? controllerClass.getSimpleName() : "UnknownController";
        return "controller:" + controller + " " + request.getRequestURI();
    }
}

