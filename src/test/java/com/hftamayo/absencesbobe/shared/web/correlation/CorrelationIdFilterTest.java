package com.hftamayo.absencesbobe.shared.web.correlation;

import com.hftamayo.absencesbobe.shared.web.constants.CorrelationConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void doFilter_whenHeaderPresent_usesHeaderValue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationConstants.HEADER, " corr-123 ");

        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();

        FilterChain chain = (servletRequest, servletResponse) ->
                mdcValueInsideChain.set(MDC.get(CorrelationConstants.MDC_KEY));

        filter.doFilter(request, response, chain);

        assertEquals("corr-123", request.getAttribute(CorrelationConstants.ATTRIBUTE));
        assertEquals("corr-123", response.getHeader(CorrelationConstants.HEADER));
        assertEquals("corr-123", mdcValueInsideChain.get());
        assertNull(MDC.get(CorrelationConstants.MDC_KEY));
    }

    @Test
    void doFilter_whenHeaderMissing_generatesCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();

        FilterChain chain = (servletRequest, servletResponse) ->
                mdcValueInsideChain.set(MDC.get(CorrelationConstants.MDC_KEY));

        filter.doFilter(request, response, chain);

        String correlationId = response.getHeader(CorrelationConstants.HEADER);

        assertNotNull(correlationId);
        assertDoesNotThrow(() -> UUID.fromString(correlationId));
        assertEquals(correlationId, request.getAttribute(CorrelationConstants.ATTRIBUTE));
        assertEquals(correlationId, mdcValueInsideChain.get());
        assertNull(MDC.get(CorrelationConstants.MDC_KEY));
    }

    @Test
    void doFilter_whenHeaderBlank_generatesCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationConstants.HEADER, "   ");

        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();

        FilterChain chain = (servletRequest, servletResponse) ->
                mdcValueInsideChain.set(MDC.get(CorrelationConstants.MDC_KEY));

        filter.doFilter(request, response, chain);

        String correlationId = response.getHeader(CorrelationConstants.HEADER);

        assertNotNull(correlationId);
        assertDoesNotThrow(() -> UUID.fromString(correlationId));
        assertEquals(correlationId, request.getAttribute(CorrelationConstants.ATTRIBUTE));
        assertEquals(correlationId, mdcValueInsideChain.get());
        assertNull(MDC.get(CorrelationConstants.MDC_KEY));
    }

    @Test
    void doFilter_whenChainThrows_stillClearsMdc() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationConstants.HEADER, "corr-error");

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (servletRequest, servletResponse) -> {
            throw new ServletException("boom");
        };

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, chain));
        assertNull(MDC.get(CorrelationConstants.MDC_KEY));
    }
}