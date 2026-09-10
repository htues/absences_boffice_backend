package com.hftamayo.absencesbobe.shared.web.correlation;

import com.hftamayo.absencesbobe.shared.web.constants.CorrelationConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request);

        request.setAttribute(CorrelationConstants.ATTRIBUTE, correlationId);
        response.setHeader(CorrelationConstants.HEADER, correlationId);
        MDC.put(CorrelationConstants.MDC_KEY, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CorrelationConstants.MDC_KEY);
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String header = request.getHeader(CorrelationConstants.HEADER);

        if (header != null && !header.isBlank()) {
            return header.trim();
        }

        return UUID.randomUUID().toString();
    }
}